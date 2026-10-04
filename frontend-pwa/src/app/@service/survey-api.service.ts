import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {forkJoin, map, Observable} from 'rxjs';
import {
  ApiResponse,
  FillRequest, LoginRequest,
  QuizStat,
  QuizSubmission, RegisterRequest, ResetPasswordRequest,
  Survey,
  SurveyQuestion,
  SurveyRequest, UserResponse
} from '../@model/survey-model';
import {environment} from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SurveyApiService {
  private readonly baseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {
  }

  /**
   * 取得所有問卷
   * 不包含題目與選項
   */
  getAllSurveys(): Observable<Survey[]> {
    return this.http.get<Survey[]>(`${this.baseUrl}/get-all`);
  }

  /**
   * 建立新問卷
   */
  createSurvey(surveyData: Partial<SurveyRequest>): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.baseUrl}/create`, surveyData);
  }

  /**
   * 取得 id 問卷
   */
  questionsById(id: number): Observable<SurveyQuestion[]> {
    return this.http.get<SurveyQuestion[]>(`${this.baseUrl}/${id}/questions`);
  }

  /**
   * 取得 更新問卷
   */
  updateSurvey(survey: Survey): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.baseUrl}/update`, survey);
  }

  /**
   * 批次刪除問卷
   */
  deleteSurvey(ids: number[]): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${this.baseUrl}/delete`, {quizIds: ids});
  }
/**
 * 填寫表單
 * */
fillQuiz(fillRequest:FillRequest):Observable<ApiResponse>{
  return this.http.post<ApiResponse>(`${this.baseUrl}/fill`,fillRequest);
  }

  /**
   *  透過問卷 id 取得該張問卷所有填答者的回答
   * */
  getQuizSubmissions(id:number):Observable<QuizSubmission[]>{
    return this.http.get<QuizSubmission[]>(`${this.baseUrl}/${id}/submissions`);
  }
  /**
   * 取得問卷統計結果
   **/
  getQuizStatistics(id:number):Observable<QuizStat>{
    return this.http.get<QuizStat>(`${this.baseUrl}/${id}/statistics`);
  }
  /**
   * 取得某人已作答過的問卷 id
   * params 會組成 ?email=xxx，email 裡的 @ 由 Angular 自動編碼
   **/
  getAnsweredQuizIds(email:string):Observable<number[]>{
    return this.http.get<number[]>(`${this.baseUrl}/answered`, {params: {email}});
  }
  //=========================== User ========================================
  register(req : RegisterRequest):Observable<ApiResponse>{
    return this.http.post<ApiResponse>(`${this.baseUrl}/register` ,req);
  }
  login(req : LoginRequest):Observable<UserResponse>{
    return this.http.post<UserResponse>(`${this.baseUrl}/login` ,req);
  }
  resetPassword(req : ResetPasswordRequest):Observable<ApiResponse>{
    return this.http.post<ApiResponse>(`${this.baseUrl}/reset-password` ,req);
  }

  // ================= 拿問卷 單筆 =============
  getSurveyDetail(id: number): Observable<Survey> {
    return forkJoin([
      this.getAllSurveys(),
      this.questionsById(id)
    ]).pipe(
      map(([survey,questions])=>{
        const target = survey.find(s=> s.id === id);
        if(!target){
          throw new Error(`Survey not found: ${id}`);
        }
        return{...target,questions}
    })
    );
  }

}
