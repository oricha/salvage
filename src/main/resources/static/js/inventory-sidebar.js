document.addEventListener('DOMContentLoaded', () => {
  const modelsByMake = window.inventoryModelsByMake || {};
  const form = document.getElementById('inventoryFilterForm');
  const brandSelect = document.getElementById('sidebarBrand');
  const modelSelect = document.getElementById('sidebarModel');
  const yearFrom = form?.querySelector('[name="yearFrom"]');
  const yearTo = form?.querySelector('[name="yearTo"]');
  let debounceTimer;

  const updateModelOptions = () => {
    if (!brandSelect || !modelSelect) {
      return;
    }

    const selectedBrand = brandSelect.value;
    const selectedModel = modelSelect.dataset.selected || '';
    const models = modelsByMake[selectedBrand] || [];
    modelSelect.innerHTML = '<option value="">choose a model</option>';

    models.forEach(model => {
      const option = document.createElement('option');
      option.value = model;
      option.textContent = model;
      option.selected = selectedModel === model;
      modelSelect.appendChild(option);
    });
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

  yearFrom?.addEventListener('change', validateYears);
  yearTo?.addEventListener('change', validateYears);
  form?.addEventListener('change', debounceSubmit);

  updateModelOptions();
});
