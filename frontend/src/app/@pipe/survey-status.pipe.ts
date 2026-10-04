import {Pipe, PipeTransform} from '@angular/core';
import {statusAdj, Survey, SurveyStatus} from '../@model/survey-model';

@Pipe({
  name: 'surveyStatus'
})
export class SurveyStatusPipe implements PipeTransform {

  transform(survey: Pick<Survey, 'isPublished' | 'startDate' | 'endDate'>): SurveyStatus {
    return statusAdj(survey);
  }

}
