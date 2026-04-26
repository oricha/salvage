document.addEventListener('DOMContentLoaded', () => {
  const modelsByMake = window.inventoryModelsByMake || {};
  const formNodes = document.querySelectorAll('[data-inventory-filter-form="true"]');

  const isMeaningfulValue = value => value !== null && value !== undefined && String(value).trim() !== '';

  const setFieldError = (field, message) => {
    if (!field) {
      return;
    }
    const errorId = field.getAttribute('aria-describedby');
    const errorNode = errorId ? document.getElementById(errorId) : null;
    field.setCustomValidity(message || '');
    field.setAttribute('aria-invalid', message ? 'true' : 'false');
    field.classList.toggle('is-invalid', Boolean(message));
    if (errorNode) {
      errorNode.hidden = !message;
      errorNode.textContent = message || errorNode.textContent;
    }
  };

  const toggleAdvancedSection = (button, section, expanded) => {
    const labelNode = button.querySelector('.inventory-advanced-toggle-label');
    const stateNode = button.querySelector('[data-advanced-toggle-state]');
    button.setAttribute('aria-expanded', String(expanded));
    button.classList.toggle('is-expanded', expanded);
    if (labelNode) {
      labelNode.textContent = expanded
        ? (button.dataset.hideLabel || 'Hide advanced filters')
        : (button.dataset.showLabel || 'Show advanced filters');
    } else {
      button.textContent = expanded
        ? (button.dataset.hideLabel || 'Hide advanced filters')
        : (button.dataset.showLabel || 'Show advanced filters');
    }
    if (stateNode) {
      stateNode.textContent = expanded
        ? (button.dataset.expandedLabel || 'Expanded')
        : (button.dataset.collapsedLabel || 'Collapsed');
    }
    section.hidden = !expanded;
    section.classList.toggle('is-expanded', expanded);
  };

  const updateBrandState = form => {
    const brandGroup = form.querySelector('.inventory-brand-group');
    const brandSelect = form.querySelector('.inventory-brand-select');
    if (!brandGroup || !brandSelect) {
      return;
    }
    brandGroup.classList.toggle('has-selected-brand', Boolean(brandSelect.value));
    brandGroup.classList.toggle('has-active-selection', Boolean(brandSelect.value));
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
    modelSelect.closest('.inventory-form-group')?.classList.toggle('has-active-selection', Boolean(modelSelect.value));
  };

  const validateYears = form => {
    const yearFrom = form.querySelector('.inventory-year-from');
    const yearTo = form.querySelector('.inventory-year-to');
    if (!yearFrom || !yearTo) {
      return true;
    }

    if (!yearFrom.value || !yearTo.value) {
      setFieldError(yearTo, '');
      return true;
    }

    const from = Number(yearFrom.value);
    const to = Number(yearTo.value);
    if (from <= to) {
      setFieldError(yearTo, '');
      return true;
    }

    setFieldError(yearTo, form.dataset.yearRangeMessage || 'Year to must be greater than or equal to year from');
    yearTo.reportValidity();
    return false;
  };

  const validateRangePair = (form, minName, maxName) => {
    const minInput = form.querySelector(`[name="${minName}"]`);
    const maxInput = form.querySelector(`[name="${maxName}"]`);
    if (!minInput || !maxInput) {
      return true;
    }
    if (!minInput.value || !maxInput.value) {
      setFieldError(maxInput, '');
      return true;
    }
    if (Number(minInput.value) <= Number(maxInput.value)) {
      setFieldError(maxInput, '');
      return true;
    }

    setFieldError(maxInput, form.dataset.rangeMessage || 'Maximum value must be greater than or equal to minimum value');
    maxInput.reportValidity();
    return false;
  };

  const validateRanges = form => {
    return [
      validateRangePair(form, 'minPrice', 'maxPrice'),
      validateRangePair(form, 'minMileage', 'maxMileage')
    ].every(Boolean);
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

  const updatePresetState = form => {
    form.querySelectorAll('.inventory-preset-chip').forEach(button => {
      const targetName = button.dataset.presetTarget;
      const targetInput = form.querySelector(`[name="${targetName}"]`);
      button.classList.toggle('is-active', Boolean(targetInput) && String(targetInput.value) === String(button.dataset.presetValue || ''));
    });
  };

  const countActiveSelections = (form, groupName) => {
    const groups = form.querySelectorAll(`[data-filter-group="${groupName}"]`);
    let count = 0;

    groups.forEach(group => {
      const controls = group.querySelectorAll('input, select, textarea');
      const active = Array.from(controls).some(control => {
        if (control.disabled) {
          return false;
        }
        if (control instanceof HTMLInputElement && control.type === 'checkbox') {
          return control.checked;
        }
        if (control instanceof HTMLSelectElement && control.multiple) {
          return Array.from(control.selectedOptions).some(option => isMeaningfulValue(option.value));
        }
        return isMeaningfulValue(control.value);
      });
      group.classList.toggle('has-active-selection', active || group.classList.contains('has-selected-brand'));
      if (active) {
        count += 1;
      }
    });

    return count;
  };

  const updateFormStatus = form => {
    const statusNode = form.querySelector('[data-filter-status]');
    const primaryNode = form.querySelector('[data-primary-count]');
    const advancedNode = form.querySelector('[data-advanced-count]');
    const stateNode = form.querySelector('[data-advanced-state]');
    const advancedToggle = form.querySelector('.inventory-advanced-toggle');

    const primaryCount = countActiveSelections(form, 'primary');
    const advancedCount = countActiveSelections(form, 'advanced');
    const isExpanded = advancedToggle?.getAttribute('aria-expanded') === 'true';

    if (primaryNode) {
      primaryNode.textContent = `${primaryCount} ${form.dataset.primarySelectedLabel || 'primary selected'}`;
    }
    if (advancedNode) {
      advancedNode.textContent = `${advancedCount} ${form.dataset.advancedSelectedLabel || 'advanced selected'}`;
    }
    if (stateNode) {
      stateNode.textContent = isExpanded
        ? (form.dataset.expandedLabel || 'Expanded')
        : (form.dataset.collapsedLabel || 'Collapsed');
    }

    const toggleCountNode = form.querySelector('[data-advanced-toggle-count]');
    if (toggleCountNode) {
      toggleCountNode.textContent = `${advancedCount} ${form.dataset.advancedSelectedLabel || 'advanced selected'}`;
    }

    if (statusNode) {
      const template = form.dataset.statusSummaryTemplate || 'Primary: {primary}. Advanced: {advanced}. State: {state}.';
      statusNode.setAttribute(
        'data-status-text',
        template
          .replace('{primary}', String(primaryCount))
          .replace('{advanced}', String(advancedCount))
          .replace('{state}', isExpanded ? (form.dataset.expandedLabel || 'Expanded') : (form.dataset.collapsedLabel || 'Collapsed'))
      );
    }

    updatePresetState(form);
  };

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

    const shouldRequestLocation = () => radiusSelect.value && (!latitudeInput.value || !longitudeInput.value);

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
      updateBrandState(form);
      updateFormStatus(form);
    });

    modelSelect?.addEventListener('change', () => {
      modelSelect.closest('.inventory-form-group')?.classList.toggle('has-active-selection', Boolean(modelSelect.value));
      updateFormStatus(form);
    });

    brandSearch?.addEventListener('input', filterBrandOptions);

    form.querySelectorAll('.inventory-year-from, .inventory-year-to, [name="minPrice"], [name="maxPrice"], [name="minMileage"], [name="maxMileage"]').forEach(input => {
      input.addEventListener('input', () => {
        setFieldError(input, '');
      });
      input.addEventListener('change', () => {
        validateYears(form);
        validateRanges(form);
        updateFormStatus(form);
      });
    });

    form.addEventListener('change', event => {
      if (!validateYears(form) || !validateRanges(form)) {
        return;
      }

      updateFormStatus(form);

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
      toggleAdvancedSection(
        advancedToggle,
        advancedSection,
        advancedToggle.getAttribute('aria-expanded') === 'true'
      );
      advancedToggle.addEventListener('click', () => {
        const expanded = advancedToggle.getAttribute('aria-expanded') !== 'true';
        toggleAdvancedSection(advancedToggle, advancedSection, expanded);
        updateFormStatus(form);
        if (expanded) {
          window.history.replaceState(null, '', '#extra');
        } else if (window.location.hash === '#extra') {
          window.history.replaceState(null, '', window.location.pathname + window.location.search);
        }
      });
    }

    if (window.location.hash === '#extra' && advancedToggle && advancedSection) {
      toggleAdvancedSection(advancedToggle, advancedSection, true);
      window.setTimeout(() => {
        advancedSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }, 50);
    }

    filterBrandOptions();
    updateModelOptions(brandSelect, modelSelect);
    updateBrandState(form);
    hydrateLocation(form);
    updateFormStatus(form);
  });
});
