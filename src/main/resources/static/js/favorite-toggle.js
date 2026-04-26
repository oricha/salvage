(function () {
  function updateToggleState(carId, isActive, activeLabel, inactiveLabel) {
    document.querySelectorAll('[data-favorite-toggle][data-car-id="' + carId + '"]').forEach(function (button) {
      button.classList.toggle('active', isActive);
      button.setAttribute('aria-pressed', String(isActive));
      var icon = button.querySelector('i');
      if (icon) {
        icon.classList.toggle('far', !isActive);
        icon.classList.toggle('fas', isActive);
      }
      var label = button.querySelector('[data-favorite-label]');
      if (label) {
        label.textContent = isActive ? activeLabel : inactiveLabel;
      }
    });
  }

  document.addEventListener('click', function (event) {
    var button = event.target.closest('[data-favorite-toggle]');
    if (!button) {
      return;
    }

    event.preventDefault();

    var carId = button.getAttribute('data-car-id');
    var activeLabel = button.getAttribute('data-label-active') || 'Guardado';
    var inactiveLabel = button.getAttribute('data-label-inactive') || 'Guardar';
    var loginUrl = button.getAttribute('data-login-url') || '/login';
    var isActive = button.classList.contains('active');
    var endpoint = isActive ? '/favorites/remove/' + carId : '/favorites/add/' + carId;

    fetch(endpoint, {
      method: 'POST',
      headers: {
        'X-Requested-With': 'XMLHttpRequest'
      },
      credentials: 'same-origin'
    }).then(function (response) {
      if (response.ok) {
        updateToggleState(carId, !isActive, activeLabel, inactiveLabel);
        return;
      }
      if (response.status === 401 || response.status === 403) {
        window.location.href = loginUrl;
        return;
      }
      return response.json().then(function (payload) {
        throw new Error(payload.message || 'No se pudo actualizar el parking.');
      }).catch(function () {
        throw new Error('No se pudo actualizar el parking.');
      });
    }).catch(function (error) {
      window.alert(error.message || 'No se pudo actualizar el parking.');
    });
  });
}());
