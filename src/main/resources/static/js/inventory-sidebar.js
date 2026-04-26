document.addEventListener('DOMContentLoaded', () => {
  const modelsByMake = window.inventoryModelsByMake || {};
  const formNodes = document.querySelectorAll('[data-inventory-filter-form="true"]');

  const toggleAdvancedSection = (button, section, expanded) => {
    button.setAttribute('aria-expanded', String(expanded));
    button.textContent = expanded
      ? (button.dataset.hideLabel || 'Hide advanced filters')
      : (button.dataset.showLabel || 'Show advanced filters');
    section.hidden = !expanded;
  };

  const updateModelOptions = (brandSelect, modelSelect) => {
    if (!brandSelect || !modelSelect) {
      return;
    }

    const selectedBrand = brandSelect.value;
    const selectedModel = modelSelect.dataset.selected || '';
    const placeholder = modelSelect.dataset.placeholder || 'choose a model';
    const models = modelsByMake[selectedBrand] || [];
    modelSelect.innerHTML = '';

    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = placeholder;
    modelSelect.appendChild(defaultOption);

    models.forEach(model => {
      const option = document.createElement('option');
      option.value = model;
      option.textContent = model;
      option.selected = selectedModel === model;
      modelSelect.appendChild(option);
    });

    modelSelect.disabled = !selectedBrand || models.length === 0;
  };

  const validateYears = form => {
    const yearFrom = form.querySelector('.inventory-year-from');
    const yearTo = form.querySelector('.inventory-year-to');
    if (!yearFrom || !yearTo || !yearFrom.value || !yearTo.value) {
      return true;
    }

    const from = Number(yearFrom.value);
    const to = Number(yearTo.value);
    if (from <= to) {
      yearTo.setCustomValidity('');
      return true;
    }

    yearTo.setCustomValidity('Year to must be greater than or equal to year from');
    yearTo.reportValidity();
    return false;
  };

  const validateRanges = form => {
    const ranges = [
      ['minPrice', 'maxPrice'],
      ['minMileage', 'maxMileage']
    ];

    return ranges.every(([minName, maxName]) => {
      const minInput = form.querySelector(`[name="${minName}"]`);
      const maxInput = form.querySelector(`[name="${maxName}"]`);
      if (!minInput || !maxInput || !minInput.value || !maxInput.value) {
        return true;
      }
      if (Number(minInput.value) <= Number(maxInput.value)) {
        maxInput.setCustomValidity('');
        return true;
      }

      maxInput.setCustomValidity('Maximum value must be greater than or equal to minimum value');
      maxInput.reportValidity();
      return false;
    });
  };

  const scheduleDesktopSubmit = (() => {
    let debounceTimer;
    return form => {
      window.clearTimeout(debounceTimer);
      debounceTimer = window.setTimeout(() => {
        if (window.innerWidth > 1024) {
          form.requestSubmit();
        }
      }, 400);
    };
  })();

  const applyPreset = (form, button) => {
    const targetName = button.dataset.presetTarget;
    const targetInput = form.querySelector(`[name="${targetName}"]`);
    if (!targetInput) {
      return;
    }
    targetInput.value = button.dataset.presetValue || '';
    targetInput.dispatchEvent(new Event('change', { bubbles: true }));
  };

  const hydrateLocation = form => {
    const radiusSelect = form.querySelector('.inventory-nearby-radius');
    const latitudeInput = form.querySelector('.inventory-reference-latitude');
    const longitudeInput = form.querySelector('.inventory-reference-longitude');
    if (!radiusSelect || !latitudeInput || !longitudeInput || !navigator.geolocation) {
      return;
    }

    const shouldRequestLocation = () => {
      return radiusSelect.value && (!latitudeInput.value || !longitudeInput.value);
    };

    const requestLocation = () => {
      if (!shouldRequestLocation()) {
        return;
      }
      navigator.geolocation.getCurrentPosition(
        position => {
          latitudeInput.value = String(position.coords.latitude);
          longitudeInput.value = String(position.coords.longitude);
        },
        () => {
          latitudeInput.value = '';
          longitudeInput.value = '';
        },
        { enableHighAccuracy: false, timeout: 5000, maximumAge: 600000 }
      );
    };

    radiusSelect.addEventListener('change', requestLocation);
    requestLocation();
  };

  formNodes.forEach(form => {
    const brandSearch = form.querySelector('.inventory-brand-search');
    const brandSelect = form.querySelector('.inventory-brand-select');
    const modelSelect = form.querySelector('.inventory-model-select');
    const advancedToggle = form.querySelector('.inventory-advanced-toggle');
    const advancedSection = form.querySelector('.inventory-advanced-section');
    const presetButtons = form.querySelectorAll('.inventory-preset-chip');

    const filterBrandOptions = () => {
      if (!brandSearch || !brandSelect) {
        return;
      }

      const query = brandSearch.value.trim().toLowerCase();
      Array.from(brandSelect.options).forEach(option => {
        if (!option.value) {
          option.hidden = false;
          return;
        }
        option.hidden = query.length > 0 && !option.textContent.toLowerCase().includes(query);
      });
    };

    brandSelect?.addEventListener('change', () => {
      modelSelect.dataset.selected = '';
      updateModelOptions(brandSelect, modelSelect);
    });

    brandSearch?.addEventListener('input', filterBrandOptions);

    form.querySelectorAll('.inventory-year-from, .inventory-year-to, [name="minPrice"], [name="maxPrice"], [name="minMileage"], [name="maxMileage"]').forEach(input => {
      input.addEventListener('change', () => {
        validateYears(form);
        validateRanges(form);
      });
    });

    form.addEventListener('change', event => {
      if (!validateYears(form) || !validateRanges(form)) {
        return;
      }

      const target = event.target;
      if (!(target instanceof HTMLSelectElement) && !(target instanceof HTMLInputElement)) {
        return;
      }
      scheduleDesktopSubmit(form);
    });

    presetButtons.forEach(button => {
      button.addEventListener('click', () => applyPreset(form, button));
    });

    if (advancedToggle && advancedSection) {
      advancedToggle.dataset.showLabel = advancedToggle.dataset.showLabel || advancedToggle.textContent.trim();
      advancedToggle.dataset.hideLabel = advancedToggle.dataset.hideLabel || advancedToggle.textContent.trim();
      toggleAdvancedSection(
        advancedToggle,
        advancedSection,
        advancedToggle.getAttribute('aria-expanded') === 'true'
      );
      advancedToggle.addEventListener('click', () => {
        const expanded = advancedToggle.getAttribute('aria-expanded') !== 'true';
        toggleAdvancedSection(advancedToggle, advancedSection, expanded);
      });
    }

    filterBrandOptions();
    updateModelOptions(brandSelect, modelSelect);
    hydrateLocation(form);
  });
});
