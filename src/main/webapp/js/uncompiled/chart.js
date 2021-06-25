(function() {
    class Chart {
        constructor(props) {
            props = props || {};

            var that = this;
            this.animate = function() {
                if (that.animating)
                    requestAnimationFrame(that.animate);
                that.redraw();
            }

            this.resize = function() {
                that.redraw();
            }

            this.redrawListeners = [];

            if (typeof props.onRedraw === "function") {
                this.redrawListeners.push(props.onRedraw);
            }

            this.animating = props.animate || false;
            if (this.animating) {
                this.animate();
            }

            if (typeof props.autoResize !== "boolean" || props.autoResize) {
                this.startResizeListener();
            }
        }

        addRedrawListener(func) {
            let i = this.redrawListeners.indexOf(func);
            if (i < 0)
                this.redrawListeners.push(func);
        }

        removeRedrawListener(func) {
            let i = this.redrawListeners.indexOf(func);
            if (i >= 0)
                this.redrawListeners.splice(i, 1);
        }

        startResizeListener() {
            window.addEventListener("resize", this.resize);
        }

        stopResizeListener() {
            window.removeEventListener("resize", this.resize);
        }

        startAnimation() {
            this.animating = true;
            this.animate();
        }

        stopAnimation() {
            this.animating = false;
        }

        redraw() {
            for (let listener of this.redrawListeners) {
                listener.call(this);
            }
        }
    }

    window.Chart = Chart;

    class CanvasChart extends Chart {
        /**
         * @param {HTMLCanvasElement} canvas 
         */
        constructor(canvas, props) {
            super(props);
            props = props || {};
            
            this.canvas = canvas;
            this.context = canvas.getContext("2d");
        }

        redraw() {
            super.redraw();
            this.canvas.width = this.canvas.clientWidth;
            this.canvas.height = this.canvas.clientHeight;
            this.render(this.canvas, this.context, this.canvas.width, this.canvas.height);
        }

        /**
         * Redraw the chart
         * @param {HTMLCanvasElement} canvas 
         * @param {CanvasRenderingContext2D} context 
         * @param {number} width
         * @param {number} height
         */
        render(canvas, context, width, height) {
        }
    }

    window.CanvasChart = CanvasChart;

    class PieChart extends CanvasChart {
        constructor(canvas, props) {
            super(canvas, props);
            props = props || {};

            this.total = 0;
            this.data = {};

            this.hollow = typeof props.hollow === "number" ? props.hollow : 0;
            this.margin = typeof props.margin === "number" ? props.margin : 10;
            this.angle = typeof props.angle === "number" ? props.angle : 0;
        }

        /**
         * Add or update data in the chart
         * @param {string} id A unique identifier that represents this data
         * @param {string} color The color of this data
         * @param {number} val The value of this data
         */
        updateData(id, color, val) {
            this.data[id] = {id: id, color: color, value: val};

            this.total = 0;
            for (let k in this.data) {
                let d = this.data[k];
                this.total += d.value;
            }

            return this;
        }

        /**
         * Remove data from the chart
         * @param {string} id The ID of the data to remove
         */
        removeData(id) {
            delete this.data[id];

            return this;
        }

        /**
         * Redraw the chart
         * @param {HTMLCanvasElement} canvas 
         * @param {CanvasRenderingContext2D} ctx 
         * @param {number} width
         * @param {number} height
         */
        render(canvas, ctx, width, height) {
            ctx.clearRect(0, 0, width, height);

            let x = width / 2;
            let y = height / 2;
            let or = Math.min(width, height) / 2 - this.margin;
            let ir = or * this.hollow;

            let a = this.angle / 360;

            let start = 0;
            for (let k in this.data) {
                let d = this.data[k];
                let end = start + d.value / this.total;
                ctx.fillStyle = d.color;
                fillPie(ctx, x, y, ir, or, start + a, end + a);
                start = end;
            }
        }
    }

    /**
     * @param {CanvasRenderingContext2D} ctx
     */
    function fillPie(ctx, x, y, innerRadius, outerRadius, start, end) {
        if (outerRadius < 0) return;
        if (innerRadius < 0) innerRadius = 0;

        let s = start * 2 * Math.PI;
        let e = end * 2 * Math.PI;
        
        ctx.beginPath();
        ctx.arc(x, y, outerRadius, s, e);
        if (innerRadius === 0) {
            ctx.lineTo(x, y);
        } else {
            ctx.arc(x, y, innerRadius, e, s, true);
        }
        ctx.closePath();
        ctx.fill();
    }

    window.PieChart = PieChart;

    
    class NamedRowChart extends CanvasChart {
        constructor(canvas, props) {
            super(canvas, props);

            this.data = {};
            this.colors = {};
            this.labels = [];

            this.min = Infinity;
            this.max = -Infinity;
            this.bounds = bounds(this.min, this.max);
            this.dataSize = 0;
            this.dataCount = 0;
        }

        recomputeMinMax() {
            let min = Infinity;
            let max = -Infinity;
            let ds = -Infinity;
            let dc = 0;

            for (let k in this.colors) {
                let d = this.data[k];

                if (d && Array.isArray(d)) {
                    min = Math.min(min, ...d);
                    max = Math.max(max, ...d);
                    ds = Math.max(ds, d.length);

                    dc ++;
                }
            }

            this.min = min;
            this.max = max;
            this.bounds = bounds(this.min, this.max);
            this.dataSize = ds;
            this.dataCount = dc;
        }

        /**
         * Set the chart labels
         */
        setLabels(...values) {
            this.labels = [...values];
            return this;
        }

        /**
         * Update data in the chart
         * @param {string} id The ID of the data
         */
        updateData(id, ...values) {
            this.data[id] = values;
            this.recomputeMinMax();
            return this;
        }

        /**
         * Add displayed data to the chart
         * @param {string} id The ID of the data to add
         * @param {string} color The color
         */
        addId(id, col) {
            this.colors[id] = col;
            this.recomputeMinMax();
            return this;
        }

        /**
         * Remove displayed data from the chart
         * @param {string} id The ID of the data to remove
         */
        removeId(id) {
            delete this.colors[id];
            this.recomputeMinMax();
            return this;
        }
    }

    window.NamedRowChart = NamedRowChart;


    function magnitude(n) {
        return Math.floor(Math.log(n) / Math.LN10 + 0.000000001); // + a tiny number because float math sucks
    }

    function exp10(n) {
        return Math.pow(10, n);
    }

    function stepFloor(n, d) {
        return Math.floor(n / d) * d;
    }

    function stepCeil(n, d) {
        return Math.ceil(n / d) * d;
    }

    function bounds(min, max) {
        let mag = Math.max(magnitude(min), magnitude(max));
        let exp = exp10(mag);

        let loR = Math.floor(min / exp);
        let hiR = Math.ceil(max / exp);
        let steps = hiR - loR;

        return {
            lo: loR * exp,
            hi: hiR * exp,
            steps: steps,
            stepSize: exp
        };
    }

    function lerp(a, b, t) {
        return (b - a) * t + a;
    }

    function unlerp(a, b, t) {
        return (t - a) / (b - a);
    }

    function relerp(a, b, p, q, t) {
        return lerp(p, q, unlerp(a, b, t));
    }


    class AreaChart extends NamedRowChart {
        constructor(canvas, props) {
            super(canvas, props);
            props = props || {};

            this.alpha = typeof props.alpha === "number" ? props.alpha : 0.4;

            this.lower = typeof props.lower === "number" ? props.lower : null;
            this.upper = typeof props.upper === "number" ? props.upper : null;
            this.steps = typeof props.steps === "number" ? props.steps : null;
            this.margin = typeof props.margin === "number" ? props.margin : 10;
            this.font = typeof props.font === "string" ? props.font : "12px IBMPlexSans";
        }

        /**
         * Redraw the chart
         * @param {HTMLCanvasElement} canvas 
         * @param {CanvasRenderingContext2D} ctx 
         * @param {number} width
         * @param {number} height
         */
        render(canvas, ctx, width, height) {
            ctx.clearRect(0, 0, width, height);

            ctx.font = this.font;
            ctx.textAlign = "left";
            ctx.textBaseline = "alphabetic";

            let innerWdt = width - this.margin * 2;
            let innerHgt = height - this.margin * 2;

            let lo, hi, steps;
            if (this.lower !== null && this.upper !== null && this.steps !== null) {
                lo = this.lower;
                hi = this.upper,
                steps = this.steps;
            } else {
                lo = this.bounds.lo;
                hi = this.bounds.hi;
                steps = this.bounds.steps;
            }

            ctx.strokeStyle = "rgba(0, 0, 0, 0.3)";
            ctx.fillStyle = "rgba(0, 0, 0, 0.7)";
            ctx.lineWidth = 2;

            for (let i = 0; i <= steps; i ++) {
                let h = relerp(0, steps, this.margin + innerHgt, this.margin, i);

                ctx.beginPath();
                ctx.moveTo(this.margin, h);
                ctx.lineTo(this.margin + innerWdt, h);
                ctx.stroke();

                let s = Math.floor(relerp(0, steps, lo, hi, i));
                ctx.fillText(s, this.margin, h - 2);
            }

            if (this.alpha > 0) {
                ctx.globalAlpha = this.alpha;
                for (let k in this.colors) {
                    let col = this.colors[k];
                    let data = this.data[k];
                    if (data && Array.isArray(data)) {
                        ctx.fillStyle = col + "";
                        
                        ctx.beginPath();
    
                        ctx.moveTo(this.margin, this.margin + innerHgt);
                        let n = 0;
                        for (let v of data) {
                            let x = relerp(0, this.dataSize - 1, this.margin, this.margin + innerWdt, n);
                            let y = relerp(lo, hi, this.margin + innerHgt, this.margin, v);
    
                            ctx.lineTo(x, y);
    
                            n ++;
                        }
    
                        ctx.lineTo(this.margin + innerWdt, this.margin + innerHgt);
                        ctx.closePath()
                        ctx.fill();
                    }
                }
                ctx.globalAlpha = 1;
            }

            for (let k in this.colors) {
                let col = this.colors[k];
                let data = this.data[k];
                if (data && Array.isArray(data)) {
                    ctx.strokeStyle = col + "";
                    
                    ctx.beginPath();

                    let fn = "moveTo";
                    let n = 0;
                    for (let v of data) {
                        let x = relerp(0, this.dataSize - 1, this.margin, this.margin + innerWdt, n);
                        let y = relerp(lo, hi, this.margin + innerHgt, this.margin, v);

                        ctx[fn](x, y);
                        fn = "lineTo";

                        n ++;
                    }

                    ctx.stroke();
                }
            }

            ctx.textAlign = "center";
            ctx.textBaseline = "top";
            ctx.fillStyle = "rgba(0, 0, 0, 0.7)";

            let n = 0;
            for (let l of this.labels) {
                let x = relerp(0, this.dataSize - 1, this.margin, this.margin + innerWdt, n);
                let y = this.margin + innerHgt + 2;

                ctx.fillText(l, x, y);

                n ++;
            }
        }
    }
    
    window.AreaChart = AreaChart;


    class BarChart extends NamedRowChart {
        constructor(canvas, props) {
            super(canvas, props);
            props = props || {};

            this.groupSpacing = typeof props.groupSpacing === "number" ? props.groupSpacing : 15;
            this.barSpacing = typeof props.barSpacing === "number" ? props.barSpacing : 2;
            this.leftMargin = typeof props.leftMargin === "number" ? props.leftMargin : 30;

            this.lower = typeof props.lower === "number" ? props.lower : null;
            this.upper = typeof props.upper === "number" ? props.upper : null;
            this.steps = typeof props.steps === "number" ? props.steps : null;
            this.margin = typeof props.margin === "number" ? props.margin : 10;
            this.font = typeof props.font === "string" ? props.font : "12px IBMPlexSans";
        }

        /**
         * Redraw the chart
         * @param {HTMLCanvasElement} canvas 
         * @param {CanvasRenderingContext2D} ctx 
         * @param {number} width
         * @param {number} height
         */
        render(canvas, ctx, width, height) {
            ctx.clearRect(0, 0, width, height);

            ctx.font = this.font;
            ctx.textAlign = "left";
            ctx.textBaseline = "alphabetic";

            let innerWdt = width - this.margin * 2;
            let innerHgt = height - this.margin * 2;

            let lo, hi, steps;
            if (this.lower !== null && this.upper !== null && this.steps !== null) {
                lo = this.lower;
                hi = this.upper,
                steps = this.steps;
            } else {
                lo = this.bounds.lo;
                hi = this.bounds.hi;
                steps = this.bounds.steps;
            }

            ctx.strokeStyle = "rgba(0, 0, 0, 0.3)";
            ctx.fillStyle = "rgba(0, 0, 0, 0.7)";
            ctx.lineWidth = 2;

            for (let i = 0; i <= steps; i ++) {
                let h = relerp(0, steps, this.margin + innerHgt, this.margin, i);

                ctx.beginPath();
                ctx.moveTo(this.margin, h);
                ctx.lineTo(this.margin + innerWdt, h);
                ctx.stroke();

                let s = Math.floor(relerp(0, steps, lo, hi, i));
                ctx.fillText(s, this.margin, h - 2);
            }

            let graphWdt = innerWdt - this.leftMargin;

            let cellWidth = graphWdt / this.dataSize;
            let groupWidth = cellWidth - this.groupSpacing;
            let barWidth = (groupWidth - this.barSpacing * (this.dataCount - 1)) / this.dataCount;

            let offset = this.groupSpacing / 2 + this.leftMargin + this.margin;
            for (let k in this.colors) {
                let col = this.colors[k];
                let data = this.data[k];

                if (data && Array.isArray(data)) {
                    ctx.fillStyle = col;

                    let fn = "moveTo";
                    let n = 0;
                    for (let v of data) {
                        let x = relerp(0, this.dataSize - 1, this.margin, this.margin + innerWdt, n);
                        let y = relerp(lo, hi, this.margin + innerHgt, this.margin, v);
                        let h = relerp(lo, hi, 0, innerHgt, v);

                        ctx.fillRect(offset + n * cellWidth, y, barWidth, h);

                        n ++;
                    }
                }

                offset += barWidth + this.barSpacing;
            }

            ctx.textAlign = "center";
            ctx.textBaseline = "top";
            ctx.fillStyle = "rgba(0, 0, 0, 0.7)";

            let n = 0;
            for (let l of this.labels) {
                let x = relerp(0, this.dataSize, this.margin, this.margin + graphWdt, n) + cellWidth / 2 + this.leftMargin;
                let y = this.margin + innerHgt + 2;

                ctx.fillText(l, x, y);

                n ++;
            }
        }
    }
    
    window.BarChart = BarChart;
}());