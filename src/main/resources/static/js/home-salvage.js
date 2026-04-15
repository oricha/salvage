document.addEventListener('DOMContentLoaded', () => {
  const brandSelect = document.getElementById('searchBrand');
  const modelSelect = document.getElementById('searchModel');
  const radiusInput = document.getElementById('radiusChoice');
  const radiusOutput = document.querySelector('.radius-output');
  const modelsByMake = window.homeModelCatalog || {};

  const updateModelOptions = () => {
    if (!brandSelect || !modelSelect) {
      return;
    }

    const selectedBrand = brandSelect.value;
    const models = modelsByMake[selectedBrand] || [];
    modelSelect.innerHTML = '<option value="">Select model</option>';
    models.forEach(model => {
      const option = document.createElement('option');
      option.value = model;
      option.textContent = model;
      modelSelect.appendChild(option);
    });
  };

  const updateRadius = () => {
    if (radiusInput && radiusOutput) {
      radiusOutput.textContent = `${radiusInput.value} km`;
    }
  };

  brandSelect?.addEventListener('change', updateModelOptions);
  radiusInput?.addEventListener('input', updateRadius);
  updateModelOptions();
  updateRadius();

  if (window.jQuery && jQuery.fn.owlCarousel) {
    jQuery('.home-arrivals-carousel').owlCarousel({
      loop: false,
      margin: 18,
      nav: true,
      dots: false,
      smartSpeed: 700,
      navText: [
        "<i class='far fa-arrow-left'></i>",
        "<i class='far fa-arrow-right'></i>"
      ],
      responsive: {
        0: { items: 1 },
        768: { items: 2 },
        992: { items: 3 },
        1200: { items: 4 }
      }
    });
  }
});
