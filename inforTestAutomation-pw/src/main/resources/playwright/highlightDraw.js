([left, top, width, height, color]) => {
	const overlay = document.createElement('div');
	overlay.setAttribute('data-pw-highlight', 'true');
	Object.assign(overlay.style, {
		position: 'fixed',
		pointerEvents: 'none',
		zIndex: '2147483647',
		border: `4px solid ${color}`,
		left: `${left - 4}px`,
		top: `${top - 4}px`,
		width: `${width + 8}px`,
		height: `${height + 8}px`,
		backgroundColor: 'transparent',
		boxSizing: 'border-box'
	});
	document.documentElement.appendChild(overlay);
};
