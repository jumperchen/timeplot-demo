if (!zk.ie8_) { // no need to support IE8 for this demo
	zk.afterMount(function () {
		var _x = {};
	zk.override(zul.wgt.Notification.prototype, _x, {
		open: function (ref, offset, position, opts) {
			if (!position)
				position = 'after_start';
			_x.open.apply(this, arguments);
		},
		_posInfo: function (ref, offset, position, opts) {
			var data = _x._posInfo.apply(this, arguments);
			var ndim = zk(this.$n()).dimension(true);
			if (offset) {
				var path = document.elementFromPoint(offset[0], offset[1]);
				var pdim = path.getBoundingClientRect();
				var width = pdim.width;
				
				data.dim.top -= 20;
				data.dim.left = pdim.left + width/2 - ndim.width/2;
			}
			return data;
		}
	});
	
	});
}