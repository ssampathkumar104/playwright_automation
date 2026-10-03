(el, color) => {
    if (!el) return;

    const rects = el.getClientRects();

    [...rects].forEach(rect => {

        // Skip invisible rects
        if (rect.width === 0 || rect.height === 0) return;

        const overlay = document.createElement('div');
        overlay.setAttribute('data-pw-highlight', 'true');

        Object.assign(overlay.style, {
            position: 'fixed',
            pointerEvents: 'none',
            zIndex: '2147483647',
            border: `4px solid ${color}`,
            background: 'transparent',
            boxSizing: 'border-box'
        });

        function updatePosition() {
            const r = el.getBoundingClientRect();
            overlay.style.left = (r.left - 4) + 'px';
            overlay.style.top = (r.top - 4) + 'px';
            overlay.style.width = (r.width + 8) + 'px';
            overlay.style.height = (r.height + 8) + 'px';
        }

        updatePosition();

        window.addEventListener('scroll', updatePosition, true);
        window.addEventListener('resize', updatePosition);

        document.body.appendChild(overlay);
    });
}
