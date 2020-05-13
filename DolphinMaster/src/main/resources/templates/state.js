$(function () {
    var option = {
        title: {
            text: '2010 ~ 2016 年太阳能行业就业人员发展情况'
        },
        yAxis: {
            title: {
                text: '就业人数'
            }
        },
        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'middle'
        },
        plotOptions: {
            series: {
                label: {
                    connectorAllowed: false
                },
                pointStart: 2010
            }
        },
        series: [{
            name: '安装，实施人员',
            data: [43934, 52503, 57177, 69658, 97031, 119931, 137133, 154175]
        }],
        responsive: {
            rules: [{
                condition: {
                    maxWidth: 500
                },
                chartOptions: {
                    legend: {
                        layout: 'horizontal',
                        align: 'center',
                        verticalAlign: 'bottom'
                    }
                }
            }]
        }
    };
    var chart1 = Highcharts.chart('appwork-size', option);
    var chart2 = Highcharts.chart('appwork-pending-size', option);
    var chart3 = Highcharts.chart('appwork-running-size', option);
});