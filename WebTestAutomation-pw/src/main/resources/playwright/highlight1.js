(el, color) => {
	const rects = el.getClientRects();
	[...rects].forEach(rect => {
		if (
			rect.top >= 0 &&
			rect.left >= 0 &&
			rect.bottom <= window.innerHeight &&
			rect.right <= window.innerWidth
		) {
			const overlay = document.createElement('div');
			overlay.setAttribute('data-pw-highlight', 'true');
			overlay.style.position = 'fixed';
			overlay.style.pointerEvents = 'none';
			overlay.style.zIndex = '2147483647';
			overlay.style.backgroundColor = 'transparent';
			overlay.style.border = '4px solid ' + color;
			overlay.style.left = (rect.left - 4) + 'px';
			overlay.style.top = (rect.top - 4) + 'px';
			overlay.style.width = (rect.width + 8) + 'px';
			overlay.style.height = (rect.height + 8) + 'px';
			document.body.appendChild(overlay);
		}
	});
}
