import { Component, OnDestroy } from '@angular/core';
import { NbCalendarRange, NbDateService } from '@nebular/theme';
import type { EChartsOption } from 'echarts';
import { Subject } from 'rxjs';

@Component({
  selector: 'tock-metrics-board',
  templateUrl: './metrics-board.component.html',
  styleUrls: ['./metrics-board.component.scss']
})
export class MetricsBoardComponent implements OnDestroy {
  destroy = new Subject();

  range: NbCalendarRange<Date>;

  constructor(protected dateService: NbDateService<Date>) {
    this.range = {
      start: this.dateService.addDay(this.monthStart, 3),
      end: this.dateService.addDay(this.monthEnd, -3)
    };
  }

  get monthStart(): Date {
    return this.dateService.getMonthStart(new Date());
  }

  get monthEnd(): Date {
    return this.dateService.getMonthEnd(new Date());
  }

  queriesStatsData = [
    ['2023-01-27', 116],
    ['2023-01-28', 129],
    ['2023-01-29', 135],
    ['2023-01-30', 86],
    ['2023-01-31', 73],
    ['2023-02-01', 85],
    ['2023-02-02', 185],
    ['2023-02-03', 105]
  ];

  queriesStatsDateList = this.queriesStatsData.map(function (item) {
    return item[0];
  });
  queriesStatsValueList = this.queriesStatsData.map(function (item) {
    return item[1];
  });

  queriesStats: EChartsOption = {
    xAxis: {
      data: this.queriesStatsDateList
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        type: 'line',
        smooth: true,
        data: this.queriesStatsValueList
      }
    ]
  };

  storiesStats: EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{c} ({d}%)'
    },
    calculable: true,
    series: [
      {
        name: 'stories',
        type: 'pie',
        radius: [30, 110],
        data: [
          { value: 9, name: 'Prêt travaux' },
          { value: 4, name: 'Apple pay' },
          { value: 3, name: 'Faire une réclamation' },
          { value: 2, name: 'Opposition sur carte/chéquier' },
          { value: 1, name: 'Code de sécurité' },
          { value: 1, name: 'Aide à la connexion' },
          { value: 1, name: 'Identifiant de connexion' },
          { value: 1, name: 'Gestion du mot de passe' },
          { value: 1, name: 'Problème de connexion' },
          { value: 1, name: 'Informations sur la localisation' },
          { value: 1, name: 'Problème de connexion' },
          { value: 1, name: 'Gestion du mot de passe' },
          { value: 1, name: 'Code de sécurité' },
          { value: 1, name: 'Identifiant de connexion' },
          { value: 0, name: 'Aide à la connexion' },
          { value: 1, name: 'Virement' }
        ]
      }
    ]
  };

  satisfactionStats: EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{c} ({d}%)'
    },
    calculable: true,
    series: [
      {
        name: 'satisfaction',
        type: 'pie',
        radius: [30, 110],
        data: [
          { value: 10, name: 'unknown' },
          { value: 15, name: 'Satisfaction OK' },
          { value: 10, name: 'Satisfaction KO' }
        ]
      }
    ]
  };

  dimensionSelected() {}

  ngOnDestroy() {
    this.destroy.next(true);
    this.destroy.complete();
  }
}
