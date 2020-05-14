$(function () {
    Highcharts.setOptions({
        global: {
            useUTC: false
        }
    });
    var option = {
        title: {
            text: 'DolphinMaster'
        },
        xAxis: {
            type: 'datetime',
            tickPixelInterval: 150
        },
        yAxis: {
            title: {
                text: '事件数'
            }
        },
        credits: {
            enabled: false // 禁用版权信息
        },
        events: {
            load: function () {
                var series = this.series[0],
                    chart = this;
                setInterval(function () {
                    var x = (new Date()).getTime(), // 当前时间
                        y = Math.random(); // 随机值
                    series.addPoint([x, y], true, true);
                }, 1000);
            }
        },
        legend: {
            enabled: false
        },
        series: [{
            name: '随机数据',
            data: (function () {
                // 生成随机值
                var data = [],
                    time = (new Date()).getTime(),
                    i;
                for (i = -19; i <= 0; i += 1) {
                    data.push({
                        x: time + i * 1000,
                        y: Math.random()
                    });
                }
                return data;
            }())
        }],
        responsive: {
            rules: [{
                condition: {
                    maxWidth: 500
                },
                chartOptions: {}

            }]
        }
    };
    var chart1 = Highcharts.chart('appwork-size', option);
    var chart2 = Highcharts.chart('appwork-pending-size', option);
    var chart3 = Highcharts.chart('appwork-running-size', option);
});