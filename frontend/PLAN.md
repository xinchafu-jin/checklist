# Survey 專案 — 待辦清單

> 掃描日期：2026-08-09
> 前端：Angular 19 standalone + Material 19 + Chart.js 4
> 後端：Spring Boot，`~/IdeaProjects/quiz1150512`， **11 支 API**（8 支問卷 + 3 支帳號）
> 資料庫：MySQL `backend`
> `ng build` 通過（initial 1.29 MB / 上限 2 MB）

---

## 現況速覽

| 頁面 | 狀態 |
|---|---|
| login | ✅ 接上 API，登入後存 `CurrentUserService` |
| register | ✅ |
| reset-password | ✅ |
| custom-survey | 🟡 欄位驗證已補完；仍可送出 0 題的問卷，成功也沒提示 |
| edit | 🟡 只對「未開始」的問卷有效；欄位驗證還停在 custom-survey 補之前的樣子 |
| release | 🟡 可用，彈窗關閉靠輪詢 |
| fill-in | 🟡 可用，`userEmail` 還寫死 |
| statistics | ✅ 兩個 tab 完整 |
| my-survey | ✅ 只列自己建的，`updatedAt` 已接真資料 |
| survey-menu | ✅ |
| dash-board | 🟡 四張卡已接真資料，待作答清單已濾成「進行中」；`surveysCount` 與「尚未作答」的語意還沒修 |

---

## 下一步（建議順序）

1. **決定 USER 登入後看到什麼** —— 紙上作業，不是寫程式。
   沒有這個答案，role 分流就沒有目的地
2. **後端 `/answered?email=`** —— USER 前台的核心是「待作答／已作答」，沒它做出來是假的
3. **role 分流 + 前台一起做** —— 這兩件事拆不開

---

## 一、user 這條線剩下的

- [ ] **依 `role` 分流** —— ADMIN 才看得到建立問卷／編輯／統計，USER 只看得到填答
      → **先決定 USER 的落地頁長什麼樣**，否則分流沒有目的地。
        目前導覽列兩項全是 ADMIN 功能，USER 登入後會看到一個對他永遠是 0 的 dashboard
- [ ] **頭像選單只做了半套** —— `home.component.html:29` 觸發鈕還是文字「Menu」，
      選單裡留著一顆沒有 `(click)` 的「Item 2」，點了不會有任何反應
      → 觸發鈕換成頭像（沿用 login/register 的 `.brand__mark` 圓形樣式 + 名字第一個字）
- [ ] **`release/:id` 掛了 guard 之後，連結就不能分享了**
      `window.open()` 開的彈窗會繼承 opener 的 `sessionStorage` 副本，所以從按鈕開得進去；
      但把網址複製到新分頁貼上就是空的，會被擋
      → 要先想清楚 release 頁是給**建立者**看的還是給**填答者**看的。
        如果是後者，它根本不該掛 guard

---

## 二、後端待修

### 訊息外洩（前端只能用字串比對硬擋）

- [ ] **重複作答** → 撞 `uk_quiz_email`，回一整串 SQL 錯誤
- [ ] **email 不在 `user` 表** → 撞 `quiz_response_ibfk_2`，同樣是 SQL 錯誤
      → 兩者都應在 `fillQuiz` 開頭主動查、丟乾淨的 `IllegalArgumentException`
- [ ] **`validateQuizTime` 兩個條件共用一句訊息**
      規則本身是對的（已開始的問卷不該改），但使用者看到
      「End date cannot be earlier than start date」會往錯的方向找
      → 拆成兩句，第二句改成「問卷已開始，無法編輯」

### 功能面

- [ ] **有作答的問卷無法編輯／刪除** —— `response_detail.question_id` 沒有 `ON DELETE CASCADE`，
      `updateQuiz` 的刪光重建會被外鍵擋下
      → 在 `updateQuiz` 開頭先查 `quiz_response`，有紀錄就丟乾淨訊息
- [ ] **`QuizResponseDto` 補 `createdAt` / `updatedAt`** —— 資料庫早就有值，
      只差 DTO 兩個欄位 + Service 兩行 setter。**最便宜的一項**
- [ ] **查「某人答過哪些問卷」** —— 例如 `GET /answered?email=`，
      dashboard 才能分「待作答／已作答」
- [ ] **單一問卷查詢 `GET /api/quiz/{id}`** —— 現在 `getSurveyDetail()` 得用 `forkJoin`
      打兩支硬湊

### 不急

- [ ] 三支帳號 API 掛在 `/api/quiz` 底下，`POST /api/quiz/register` 語意怪 → 搬到 `/api/user`
- [ ] user 邏輯寫在 `QuizService`（已 600+ 行）→ 拆出 `UserService`
- [ ] `/submissions`、`/statistics` 沒有權限控管，任何人看得到所有填答者 email
- [ ] 兩支查詢 API 都是 N+1 查詢
- [ ] `getLastInsertedId()` 在兩個 repository 重複定義

---

## 三、前端功能收尾

### dash-board

- [ ] **待作答清單的 `element.creatorName`**（`.html:116`）—— 前端早就綁好了，
      後端補欄位後應該自動生效，**但後端沒起來，還沒實測**
- [ ] `surveysCount` 是 `survey.length`（含儲存中），與四張卡加總對不起來
- [ ] **`quantity` 還是不等於「尚未作答」** —— 已經從「全部問卷數」改成「進行中的份數」，
      跟清單對得起來了，但**沒有排除他已經答過的**
      → 要等後端 `GET /answered?email=`。在那之前畫面上的「尚未作答」其實是「可作答」
- [ ] `res: any` + `Array.isArray(res) ? res : res.quizVoList` 是多餘防禦，掩蓋真正的型別錯誤

### custom-survey / edit

- [ ] **擋住入口，不要讓人走進去才被退**
      進行中／已結束的問卷本來就不該編輯（規則是對的），
      但現在使用者可以點進編輯頁改半天，按送出才被打回來
      → my-survey 那兩個 tab 不要給「修改」按鈕，或給了但 disabled
- [ ] **edit 頁還沒補**（custom-survey 已做完，兩頁又長得不一樣了）
      `description` / `startDate` / `endDate` 三個欄位沒星號、沒錯誤訊息，日期也沒有 `min`
      → edit 的 `min` 不能照抄 `today`：編輯的是已存在的問卷，
        原本的開始日可能就在今天之前，用 `today` 會讓合法的舊值變成選不回來
- [ ] 至少一題才能送出（目前 0 題也能過驗證）
      → `questions` FormArray 掛「長度 > 0」的驗證器，比在 `subForm()` 手動檢查好
- [ ] 送出成功沒有任何提示，直接導頁
- [ ] **`[min]` 只是 UI 限制，擋不住 API** —— devtools 改掉或直接打後端都能送出過去的日期
      → 真要保證得靠後端；`validateQuizTime` 已經有這條規則，前端這層只是少踩一次
- [ ] **兩頁有約 90 行逐字相同的程式碼**（六個方法）→ 抽成共用 base class 或 service
      → **差距正在擴大**：custom-survey 的 template 補了驗證，edit 沒有。
        愈晚抽，兩邊要對齊的東西愈多

### 其他

- [ ] my-survey `openRelease()` 用 `setInterval` 每 0.5 秒輪詢彈窗是否關閉
      → 兩個毛病：使用者不關就一直跑、元件銷毀時 `clearInterval` 不會被呼叫
      → 正解是 release 頁 `postMessage()` 主動通知
- [ ] release 的 `window.close()` 在非 script 開啟的視窗會被瀏覽器擋，沒有 fallback
- [ ] survey-menu 統計欄改「已結束才顯示 icon，其餘顯示『尚未統計』」
- [ ] statistics `totalRespondents` 為 0 時是空白圓環，沒有空狀態
- [ ] fill-in 送出成功沒有提示

---

## 四、型別與體質

- [ ] `createSurvey(surveyData: Partial<SurveyRequest>)` —— `Partial` 等於放棄型別保護
- [ ] `ApiResponse` 兩個欄位都 optional，等於沒型別
- [ ] **`FillAnswer` 三個欄位全 optional** —— 之前那個 `optionId` / `optionIds` 的 typo
      就是因此逃過編譯檢查
      → 改成 union type：TEXT 一種形狀、選擇題一種形狀
- [ ] `delApi` interface 沒有任何地方使用，可刪
- [ ] `statusAdj` 上方註解寫「在此從未使用」，其實正在用
- [ ] `subscribe((res: any) =>` 散在 dashboard 與 survey-menu
- [ ] `baseUrl` 寫死 `http://localhost:8080` → 抽到 `environments/environment.ts`
      （之後要用手機測試就一定會需要）

---

## 五、建置與基礎設施

- [ ] **custom-survey 與 edit 的共通樣式抽到 `styles.scss`** —— 兩頁樣式高度重複，
      當初撞 budget 的就是它們。budget 已放寬到能過，但重複本身還在
- [ ] wildcard `**` → 404 route（目前打錯網址整頁空白）
- [ ] 全域 loading / error（後端沒開時只有 console 看得到）
- [ ] 刪掉沒被路由到的 `test` 元件
- [ ] 測試基礎設施等於沒有：幾支 spec 都是 CLI 樣板無斷言

---

## 六、之後的大工程

- [ ] **JWT** —— 現在後端不知道請求是誰發的，`SecurityConfig` 全 `permitAll`。
      畫面分流擋得住手滑，擋不住 curl
      → 適合最後做：**加 Interceptor 就能一次套用到所有請求**，
        既有的十幾支 service 方法一行都不用改
- [ ] 前後台資料夾拆分（`auth` / `admin` / `fill`）
      → 等 AuthGuard 定案，guard 的分界就是資料夾的分界
- [ ] 寄信功能（真正的忘記密碼流程）—— 目前是 email + 手機雙重比對的簡化版，
      知道別人這兩項就能改他的密碼

---

# 參考資料

## 已確認的後端行為（讀原始碼 + curl 實測）

1. `QuizRequest` 吃 body 裡的 `id`，`/update` 靠它辨識
2. `/update` 接受含 `questions` 的完整物件，題目**整批砍掉重建**
   → form 不需要存 `q.id` / `o.id`；但每次更新題目 id 都會換一批
3. `validateQuizTime` 在 `create` 和 `update` 都跑，
   規則是「結束日不早於開始日」**或**「開始日不早於今天」
   → 已開始的問卷編輯不了。**規則是對的，訊息是錯的**
4. `/submissions` 的 `answers` 長度**必定等於題數** ——
   Service 遍歷 `questions` 產生，沒作答的題目也會回
   → 可以安心靠陣列順序對齊
5. `/statistics` 的百分比分母是**總填答人數**
   → 多選題會超過 100%、選填的單選題會少於 100%
6. `quiz.created_at` / `updated_at` 資料庫早就有值且正常運作，缺的只是 DTO
7. 錯誤一律回 **400**，格式 `{success:false, message:"..."}`
8. 登入回 `{email, name, role}`，**不含密碼與手機**

## 資料庫

| 表 | 筆數 |
|---|---|
| `quiz` | 43 |
| `user` | 8 |
| `quiz_response` | 6 |

### 關鍵約束

```sql
quiz_response.user_email → user.email          ON UPDATE CASCADE
                                               ← 作答者必須是註冊使用者
quiz_response (quiz_id, user_email)  UNIQUE uk_quiz_email
                                               ← 一人一份只能答一次
question.quiz_id → quiz.id                     ON DELETE CASCADE
question_option.question_id → question.id      ON DELETE CASCADE
response_detail.response_id → quiz_response.id ON DELETE CASCADE

response_detail.question_id → question.id      ❌ 無 CASCADE ← 擋住問卷編輯
response_detail.option_id → question_option.id ❌ 無 CASCADE
quiz_response.quiz_id → quiz.id                ❌ 無 CASCADE ← 擋住問卷刪除
```

`user.role` 是 `VARCHAR(20) NOT NULL DEFAULT 'USER'`，entity 用 `@Enumerated(EnumType.STRING)`。

## 測試帳號

| email | 密碼 | 角色 |
|---|---|---|
| user@mail.com | `user1234` | **ADMIN**（建南） |
| ming@mail.com | `user1234` | USER |
| hua@mail.com / mei@mail.com / jay@mail.com | `user1234` | USER |
| test@mail.com | `newpass123` | USER |

手機號碼：建南 `0912345678`、小明 `0911111111`、小華 `0922222222`、
小美 `0933333333`、阿傑 `0944444444`、測試 `0955555555`

## 測試資料

| 用途 | 問卷 id |
|---|---|
| **測統計／作答紀錄** | **61**（5 題，三種題型都有，5 人填過） |
| 測作答 | 57～60 |
| 測編輯 | 48～55（開始日在未來，不會被日期驗證擋） |
| 別用 | 41～43（題目數 0，`/fill` 會被 `@NotEmpty` 擋） |

作答過的問卷就編輯不了，所以測作答和測編輯要用不同批。

```bash
# 重測 61 號的作答
mysql -uroot -proot quiz1150512 -e "DELETE FROM quiz_response WHERE quiz_id=61;"
```

> ⚠️ 用 mysql CLI 插中文一定要加 `--default-character-set=utf8mb4`，
> 否則會雙重編碼 —— 用同一個錯誤連線讀回來看起來是對的，API 讀出來卻是亂碼。

---

# 完成日誌

## 2026-08-09

**dashboard 待作答清單只留「進行中」**

- 篩選塞在**既有的那輪迴圈**裡（`統計` 分支順手 `answerable.push()`），
  `statusAdj()` 每份問卷仍然只呼叫一次
- **「已發佈」不等於「進行中」** —— `isPublished` 為 true 的還包含尚未開始與已結束。
  只濾掉草稿的話，清單裡會出現寫著「截止 2026-07-01」還配一顆「前往作答」的卡片
- `quantity` 改用 `answerable.length`，否則會出現「尚未作答 43 份」但清單只有 5 筆
- 補 `.survey-list__empty` 空狀態
  → **這是篩選的必要配套**：原本 43 份全列，清單不可能空；
    濾成進行中之後「空」變成常態，沒有空狀態就只剩標題配一個「0 of 0」的分頁器

**custom-survey 的欄位驗證補完**

- `startDate` 加 `[min]="today"`，`today` 是 `.ts` 的欄位（`YYYY-MM-DD` 字串）
  → `min` 是**原生 HTML 屬性**，只能寫在 template。
    一度寫進 `fb.group()` 裡（`startDate: ['', Validators.required, [min]="today"]`），
    那是 template 語法混進 `.ts`，直接編譯失敗
- `endDate` 的 `min` 綁 `form.controls.startDate.value || today` 而不是 `today`
  → 光擋「不能選過去」還是選得出「結束早於開始」，後端 `validateQuizTime` 會退，
    但使用者要按了送出才知道
- `description` / `startDate` / `endDate` 三個欄位補上星號 + `field-error` 訊息，
  照 `title` 已有的 `aria-invalid` / `aria-describedby` 模式
- 清掉 `import {min} from 'rxjs'`（IDE 自動補的，跟這件事無關）

→ **edit 頁沒動**，同樣三個欄位還是裸的，已列回待辦

**`creator_email` 打通（卡最久的一項）**

- DDL：`quiz` 加 `creator_email VARCHAR(100)` + 外鍵 `fk_quiz_creator` → `user(email)`，
  `ON UPDATE CASCADE`
- 後端 `Quiz` entity / `QuizRequest` / `QuizResponseDto` 加 `creatorEmail`，
  `QuizRepository.insertQuiz` 的 SQL 補欄位與參數
- **`updateQuiz` 刻意不加** —— 加了的話編輯者會蓋掉原製作者。
  製作者是建立當下就固定的事實，不該隨編輯行為改變
- 前端 `Survey` / `SurveyRequest` 補 `creatorEmail`，`Survey` 另有唯讀的 `creatorName`
  （後端從 `user` 表查來，只給顯示用）
- custom-survey 送出時帶 `currentUser.get()?.email`，取不到就擋下不送

→ 解鎖了 dashboard 四張卡、`creatorName`、my-survey 與 survey-menu 的篩選，
  四項都還沒做，已移到「三、前端功能收尾」

**三頁接上 `creatorEmail`（`creator_email` 解鎖的四項做掉三項）**

- dashboard「我的問卷」五個 `my*` 欄位接上真資料
  → 十個計數在**同一輪迴圈**裡算完，`statusAdj()` 每份問卷只呼叫一次
  → 改成累加式之後**必須先歸零**，否則重抓一次就翻倍
- my-survey 四個 tab 只列自己建的
- survey-menu 加「全部／我的」切換，與關鍵字、日期是三個獨立條件
  → 標題與份數跟著 scope 走，份數改用 `dataSource.data.length`（篩選後）而非 `surveys.length`

**沒登入時的行為統一成「不給」而不是「全給」**

三頁都是 `currentUser.get()?.email ?? ''`，拿不到 email 時：
dashboard 四張卡全 0、my-survey 顯示「找不到登入資訊」、survey-menu 切到「我的」得到空清單。
反過來把所有人的問卷當成自己的列出來，比空畫面糟得多。

**my-survey 補上 `loadError`**

`upData()` 原本**沒有 error 分支**。篩掉別人的問卷之後，空清單變成有歧義的畫面
——「我還沒建過」與「根本沒載到」長得一模一樣，所以這個欄位是篩選的必要配套，不是順手加的。

**AuthGuard + 登出**

- `@guard/auth.guard.ts`，`CanActivateFn`（函式式，不是舊的 class-based）
- 掛在 `home` 父路由與 `release/:id`；`''`、`register`、`reset-password` 三個沒掛
- **擋下來回傳 `UrlTree` 而不是 `false`** —— 回 `false` 只是取消導航，
  使用者會停在原地、網址不變、畫面不動，看起來像當掉
- `inject()` 與 `get()` 都必須在 guard 函式**內**：
  放在模組頂層會 `NG0203`（那時還沒有 injection context），
  而且 `get()` 在頂層只會讀一次，登入登出後拿到的都是啟動當下那個值
- home toolbar 加 `mat-menu`，登出 = `clear()` + `navigate(['/'])`

→ 登出與 guard 是配套：沒有 guard 的話，登出後按瀏覽器上一頁
  會回到 dashboard 而且看起來還登著

**兩處寫死值換成真的登入者**

- dashboard 的「建南」→ `userName`，在 `ngOnInit` 取一次存成欄位。
  用 getter 的話每次變更偵測都會讀一次 sessionStorage + `JSON.parse`
- fill-in 的 `'user@mail.com'` → `currentUser.get()?.email`，
  **並在送出前擋掉空值**：`quiz_response.user_email` 有外鍵指向 `user` 表，
  送空字串會撞 `quiz_response_ibfk_2`，使用者只會看到一整串 SQL 錯誤

**建置**

- `angular.json` budget 放寬成 `initial: 1.5MB/2MB`、`anyComponentStyle: 10kB/16kB`
- `ng build`（production）通過，initial 1.26 MB、transfer 263 kB

## 2026-08-07

**帳號系統（後端 3 支 + 前端 3 頁）**

- 後端 `register` / `login` / `reset-password` 三支 API，`UserRepository` 加四個
  `findBy` / `existsBy`（Spring Data 依方法名自動產生查詢，不用寫 SQL）
- 密碼用 `BCryptPasswordEncoder` 雜湊（60 字元），**資料庫沒有明文**
- **登入的「查無帳號」與「密碼錯誤」刻意回同一句** —— 分開講等於讓人枚舉哪些 email 已註冊；
  重設密碼的兩個失敗分支同理
- `ValidationMsg` 拆成「欄位驗證」與「業務規則」兩區
  （原本四個 USER 常數全是複製沒改的 `"Name cannot be blank"`）
- `user.role` 欄位 + `UserRole` enum，`register` 寫死 `USER`、`login` 回傳實際角色
  → **`RegisterRequest` 刻意不含 `role`**，否則任何人註冊時填 ADMIN 就變管理員
- `RegisterRequest.age` 從 `int` 改 `Integer` —— 原始型別沒填會變 0，「0 歲」和「沒填」不同
- 前端 `CurrentUserService`（`sessionStorage`）、login 頁接上 API、
  register 與 reset-password 兩頁完成
- 三支 API 用 curl 測過 12 種情境（含密碼雜湊、重複註冊、密碼錯、手機不符、欄位驗證）

**修掉的 bug**

- `resetPassword` 用 `findByPhone` 卻傳 email —— 型別都是 `String` 所以編譯過，功能永遠失敗
- 端點 `/rest-password` 少一個 e
- `login` 一度把 role 寫死成 `USER`，ADMIN 登入也拿到 USER

## 2026-08-06

**statistics 完成**

- 單選圓餅圖（Chart.js）、多選 CSS grid 長條、簡答清單
- **`@ViewChildren('chartCanvas').changes` 取代 `setTimeout`** ——
  原本「圓餅圖有時候畫不出來」是 race：`setTimeout(0)` 在賭 Angular 已經渲染完，
  但加了 `mat-tab-group` 之後多了動畫的變數
- 圓餅圖補「未作答」灰色扇形，**圖例同步加一項**
  （圖上有、圖例沒有的話，使用者會看到不明的灰色區域）
  只對單選有效 —— 多選一人可投多票，相減沒有意義
- 人員 tab 用 `mat-accordion` 摺疊面板，不用交叉表
  （題目多的時候交叉表會橫向爆版：30 題 × 150px ≈ 4500px）
- 兩支 API **各自**一個錯誤欄位，不共用 ——
  統計掛了、作答紀錄還在的話，沒理由把還能看的那個 tab 一起收掉

**fill-in 完成**

- 三種題型的必填驗證：TEXT 和 SINGLE 用 `Validators.required`，
  **MULTI 要自訂 validator** —— `[false,false]` 對 required 而言是「有值」，擋不住
- 建立「載入三態」模式（正常／載入失敗／載入中），其他頁面照抄
- **`loadError` 與 `errorMessage` 必須分開** ——
  `errorMessage` 的顯示區塊本身就在 `@if (survey)` 裡面，
  共用的話值設了、DOM 卻不存在，使用者依然看到空白

**其他**

- my-survey 四個 tab 各自接上分頁器（原本三個是裝飾品）、刪除改成兩段式確認
- survey-menu `takeUntilDestroyed` 退訂、巢狀 subscribe 改 `switchMap`
  → 關鍵好處是**錯誤處理只寫一次**，巢狀版的內層沒有 error 分支，重抓失敗會靜默
- 刪除後改叫 `applyFilters()` 而非直接覆蓋 `dataSource.data`，
  否則使用者當下的篩選條件會被洗掉

## 2026-08-05

**後端新增 `/submissions` 與 `/statistics` 兩支**

- 六個 response DTO + 兩個 repository 查詢 + Service 兩個方法
- 統計頁的封鎖解除（此前是死路，前端怎麼寫都是假資料）

**讀了後端原始碼與實際 DB schema**，確認五件此前只能推測的事：
日期驗證在 update 也跑、`/submissions` 的 answers 長度必定等於題數、
統計百分比的分母、`created_at` / `updated_at` 早就有值、
外鍵沒 CASCADE 會擋住編輯

**三個致命 bug**

- edit 呼叫 `createSurvey` 而非 `updateSurvey`（按儲存會再建一份新問卷）
- fill-in 單選 `optionId` → `optionIds`
- fill-in 送出後沒有 `next` 導頁也沒有 `error` 顯示

**custom-survey / edit 清理**

- `type="TEXT"` → `type="text"`（`TEXT` 不是合法的 input type，
  瀏覽器 fallback 成 `text` 才沒壞 —— 能動是容錯，不是正確）
- `removQuestion` / `removOption` 補上 e（兩頁共八處）
- `subForm()` 清空 `errorMessage` 補在 guard **之後**
  （放之前會把 guard 剛設好的訊息洗掉）
