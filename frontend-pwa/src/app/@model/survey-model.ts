export interface Survey {
  id: number;
  title: string;
  description: string;
  endDate: string;        // LocalDate  → "2026-08-18"
  startDate: string;      // LocalDate  → "2026-08-18"
  isPublished: boolean;
  creatorEmail: string;   // 製作者 email，用來比對「這份是不是我建的」
  creatorName: string;    // 製作者姓名，後端從 user 表查來的，只給顯示用
  createdAt: string;      // LocalDateTime → "2026-08-03T02:53:54"，顯示要用 date pipe
  updatedAt: string;      // 同上；後端 updateQuiz 會明確寫入，按過儲存就會更新
  questions?: SurveyQuestion[];
}

export interface SurveyRequest {
  title: string;
  description: string;
  startDate: string;
  endDate: string;
  isPublished: boolean;
  creatorEmail: string;   // 建立問卷時必須帶，後端會擋空值
  questions: SurveyQuestion[];
}

export interface SurveyQuestion {
  id: number;
  title: string;
  type: QuestionType;
  isRequired: boolean;
  questionNum: number;
  options: SurveyOptions[];
}

export interface SurveyOptions {
  id: number;
  optionCode: string;
  optionText: string;
}

export interface ApiResponse {
  message?: string;
  success?: boolean;
}

export interface delApi {
  quizIds: number[];
}

export interface FillRequest {
  quizId: number,
  userEmail: string,
  answers: FillAnswer[],
}

export interface FillAnswer {
  answerText?: string;
  optionIds?: number[];
  questionId?: number;

}

// ====================================
export interface QuizSubmission {
  responseId: number;
  quizId: number;
  userEmail: string;
  submittedAt: string;          // LocalDateTime → "2026-08-05T00:37:54"
  answers: SubmissionAnswer[];
}

export interface SubmissionAnswer {
  questionId: number;
  questionNum: number;
  questionTitle: string;
  questionType: QuestionType;
  options: SubmissionOption[] | null;   // 簡答題是 null
  answerText: string | null;            // 選擇題是 null
}

export interface SubmissionOption {
  optionId: number;
  optionCode: string;
  optionText: string;
  isSelected: boolean;
}

// ===================================
export interface QuizStat {
  quizId: number;
  quizTitle: string;
  totalRespondents: number;
  questionStats: QuestionStat[];
}

export interface QuestionStat {
  questionId: number;
  questionNum: number;
  questionTitle: string;
  questionType: QuestionType;
  optionStats: OptionStat[] | null;   // 簡答題是 null
  textAnswers: string[] | null;       // 選擇題是 null
}

export interface OptionStat {
  optionId: number;
  optionCode: string;
  optionText: string;
  selectedCount: number;
  percentage: number;                 // BigDecimal → number
}

// ============================ user ===================================
export interface RegisterRequest {
  name: string;
  phone: string;
  password: string;
  email: string;
  age: number |null;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface ResetPasswordRequest {
  email: string;
  phone: string;
  newPassword: string;
}

export interface UserResponse {
  email: string;
  name: string;
  role : UserRole;
}
export type UserRole = 'ADMIN' | 'USER';


//==================================================
export type QuestionType = 'SINGLE' | 'MULTI' | 'TEXT';

export type SurveyStatus = '儲存中' | '尚未開始' | '進行中' | '已結束' | '未知';

//=======================狀態判斷方法====================================================
// Pick 可以在介面選其中幾個 js的工具 在此從未使用 且在此紀錄
export function statusAdj(survey: Pick<Survey, 'isPublished' | 'startDate' | 'endDate'>): SurveyStatus {
  if (!survey.isPublished) {
    return '儲存中';
  }
  const now = new Date();
  const startDate = new Date(survey.startDate);
  startDate.setHours(0, 0, 0, 0);
  const endDate = new Date(survey.endDate);
  endDate.setHours(23, 59, 59, 999);
  if (now < startDate) {
    return '尚未開始';
  }
  if ((now >= startDate && now <= endDate) && (survey.isPublished)) {
    return '進行中';
  }
  if (now > endDate && survey.isPublished) {
    return '已結束';
  }
  return '未知';
}

// ============================ 單筆問卷 byId ===========================
