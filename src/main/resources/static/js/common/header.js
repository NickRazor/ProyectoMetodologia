/* filepath: /c:/Users/Rayson/Escritorio/ProyectoGrupalMetodologia/HappyMarket/src/main/resources/static/js/common/header.js */
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded'); // Para debugging

    const countrySelect = document.getElementById('countrySelect');
    const selectTrigger = document.getElementById('selectTrigger');
    const selectOptions = document.querySelectorAll('.select-option');

    console.log('Elements found:', {
        countrySelect: !!countrySelect,
        selectTrigger: !!selectTrigger,
        optionsCount: selectOptions.length
    });

    if (selectTrigger) {
        selectTrigger.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            console.log('Trigger clicked');
            countrySelect.classList.toggle('active');
        });
    }

    selectOptions.forEach(option => {
        option.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            console.log('Option clicked');

            const flag = this.querySelector('img').src;
            const text = this.querySelector('span').textContent;

            // Actualizar el trigger
            const triggerImg = selectTrigger.querySelector('img');
            const triggerSpan = selectTrigger.querySelector('span');
            
            if (triggerImg && triggerSpan) {
                triggerImg.src = flag;
                triggerSpan.textContent = text;
            }

            countrySelect.classList.remove('active');
        });
    });

    // Cerrar al hacer clic fuera
    document.addEventListener('click', function(e) {
        if (countrySelect && !countrySelect.contains(e.target)) {
            countrySelect.classList.remove('active');
        }
    });
});