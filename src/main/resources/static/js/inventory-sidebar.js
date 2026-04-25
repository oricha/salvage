document.addEventListener('DOMContentLoaded', () => {
  const modelsByMake = window.inventoryModelsByMake || {};
  const form = document.getElementById('inventoryFilterForm');
  const brandSearch = document.getElementById('sidebarBrandSearch');
  const brandSelect = document.getElementById('sidebarBrand');
  const modelSelect = document.getElementById('sidebarModel');
  const yearFrom = form?.querySelector('[name="yearFrom"]');
  const yearTo = form?.querySelector('[name="yearTo"]');
  let debounceTimer;

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

  const updateModelOptions = () => {
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

  const validateYears = () => {
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

  const debounceSubmit = event => {
    if (!form || !validateYears()) {
      return;
    }

    const target = event.target;
    if (!(target instanceof HTMLSelectElement) && !(target instanceof HTMLInputElement)) {
      return;
    }

    window.clearTimeout(debounceTimer);
    debounceTimer = window.setTimeout(() => {
      if (window.innerWidth > 1024) {
        form.requestSubmit();
      }
    }, 400);
  };

  brandSelect?.addEventListener('change', () => {
    modelSelect.dataset.selected = '';
    updateModelOptions();
  });
  brandSearch?.addEventListener('input', filterBrandOptions);

  yearFrom?.addEventListener('change', validateYears);
  yearTo?.addEventListener('change', validateYears);
  form?.addEventListener('change', debounceSubmit);

  filterBrandOptions();
  updateModelOptions();
});
