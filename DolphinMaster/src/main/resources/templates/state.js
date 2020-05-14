$(function () {
    var option = {
        title: {
            text: 'DolphinMaster'
        },
        yAxis: {
            title: {
                text: '事件数'
            }
        },
        credits: {
            enabled: false // 禁用版权信息
        },
        series: [{
            data: [43934, 52503, 57177, 69658, 97031, 119931, 137133, 154175],
            showInLegend: false
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