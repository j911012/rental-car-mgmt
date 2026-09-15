# レンタカー管理システム(業者側) 要件書

## 1. 目的

Spring Boot + MyBatis + JUnit の実践的なキャッチアップを目的とした学習用アプリケーション。
実務(SES案件)で採用されている以下の要素を意図的に再現する。

- MyBatis による DB アクセス(Entity / Mapper 構成)
- JUnit によるレイヤーごとのテスト
- 意味の分かる名前(Car / Reservation ベース)による URL・Controller・テンプレートの一貫した命名
- サイドバーを持つ業務システム型の CRUD 画面

## 2. スコープ

### 対象(フェーズ1)

レンタカー業者が使用する **社内管理システム(システムA)** のみ。

### 対象外

| 項目                             | 理由                                               |
| -------------------------------- | -------------------------------------------------- |
| 顧客向け予約サイト(システムB)    | フェーズ2として切り出し。まず1システムを完走させる |
| 認証・認可(ログイン、権限制御)   | 学習目的から外れ、実装が重くなるため               |
| 料金計算・請求                   | 業務ロジックが膨らむため                           |
| 車種マスタ等の追加マスタ         | テーブル数を増やさず、実装に集中するため           |
| 稼働率などの集計・ダッシュボード | 集計SQLは別テーマとして独立させる                  |

## 3. 技術スタック

| 区分           | 採用技術                                                         | 備考                                                       |
| -------------- | ---------------------------------------------------------------- | ---------------------------------------------------------- |
| 言語           | Java 17                                                          | Gradle/Spring Boot との互換性が安定                        |
| フレームワーク | Spring Boot 4.0.8                                                | Spring Initializr上でMyBatis Frameworkと互換性のある安定版 |
| ビルドツール   | Maven                                                            | Udemy課題と同じ構成で余計なハマりを避ける                  |
| DB アクセス    | MyBatis (`mybatis-spring-boot-starter`)                          | 実務と同じ方式                                             |
| テンプレート   | Thymeleaf                                                        | 既習のため                                                 |
| DB             | MySQL(DBeaverで接続)                                             | 開発用`rental`、テスト用`rental_test`の2スキーマ           |
| テスト         | JUnit 5 / Mockito / AssertJ / `mybatis-spring-boot-starter-test` |                                                            |

## 4. 命名規則

画面ID(`C1000List`のような記号的な命名)は、実務で使われる理由(画面数が多い・チーム開発でチケットと対応付けたい、など)が今回の学習規模には当てはまらず、可読性を落とすだけと判断し不採用とした。**エンティティ名(Car / Reservation)をベースにした、意味の分かる名前**に統一する。

### 命名の対応表

| 画面                        | URL                  | Controller                    | テンプレート                        |
| --------------------------- | -------------------- | ----------------------------- | ----------------------------------- |
| 車両一覧(CarList)           | `/cars`              | `CarListController`           | `templates/car/list.html`           |
| 車両登録(CarRegist)         | `/cars/new`          | `CarRegistController`         | `templates/car/regist.html`         |
| 車両編集(CarEdit)           | `/cars/{carId}/edit` | `CarEditController`           | `templates/car/edit.html`           |
| 貸出一覧(ReservationList)   | `/reservations`      | `ReservationListController`   | `templates/reservation/list.html`   |
| 貸出登録(ReservationRegist) | `/reservations/new`  | `ReservationRegistController` | `templates/reservation/regist.html` |

> 返却処理は専用画面を持たず、貸出一覧(ReservationList)からの POST アクション(`/reservations/{reservationId}/return`)として、`ReservationListController`内に実装する。

### 画面文言の管理

画面のタイトル・ラベル・ボタン文言は HTML / Java に直書きせず、`src/main/resources/messages.properties` に集約する(実務で見られた i18n の仕組みを再現。ただし英語版は作らず `LocaleResolver` の実装も行わない)。

- キー名: `画面名(camelCase).項目名`(例: `carList.title`, `carRegist.submit`)、画面共通のボタン等は `common.` プレフィックス(例: `common.regist`, `common.edit`, `common.delete`)
- Thymeleaf側は `th:text="#{キー名}"` で参照(HTMLタグを含む文言のみ `th:utext`)

### パッケージ構成

Controller / Service / Mapper のレイヤー単位で切る(package by layer)。実務の業務システムで一般的な構成に合わせる。

```
com.example.rental
├── controller
│   ├── CarListController.java
│   ├── CarRegistController.java
│   ├── CarEditController.java
│   ├── ReservationListController.java
│   └── ReservationRegistController.java
├── service
│   ├── CarService.java
│   └── ReservationService.java
├── mapper
│   ├── CarMapper.java
│   └── ReservationMapper.java
├── entity
│   ├── Car.java
│   ├── Customer.java
│   └── Reservation.java
├── dto
│   └── ReservationDetailDto.java   // JOIN 結果用
└── form
    ├── CarForm.java
    ├── CarSearchForm.java
    ├── ReservationForm.java
    └── ReservationSearchForm.java
```

`resources/mapper/` 配下に `CarMapper.xml`, `ReservationMapper.xml` を配置する。

## 5. テーブル定義

### car(車両)

| カラム       | 型          | 制約               | 説明                                   |
| ------------ | ----------- | ------------------ | -------------------------------------- |
| car_id       | INT         | PK, AUTO_INCREMENT |                                        |
| car_name     | VARCHAR(50) | NOT NULL           | 車種名                                 |
| number_plate | VARCHAR(20) | NOT NULL, UNIQUE   | ナンバー                               |
| status       | VARCHAR(10) | NOT NULL           | `AVAILABLE` / `RENTED` / `MAINTENANCE` |
| created_at   | DATETIME    | NOT NULL           |                                        |
| updated_at   | DATETIME    | NOT NULL           |                                        |

### customer(顧客)

| カラム        | 型          | 制約               | 説明 |
| ------------- | ----------- | ------------------ | ---- |
| customer_id   | INT         | PK, AUTO_INCREMENT |      |
| customer_name | VARCHAR(50) | NOT NULL           |      |
| phone         | VARCHAR(20) | NOT NULL           |      |
| created_at    | DATETIME    | NOT NULL           |      |
| updated_at    | DATETIME    | NOT NULL           |      |

### reservation(貸出)

| カラム         | 型          | 制約                    | 説明                  |
| -------------- | ----------- | ----------------------- | --------------------- |
| reservation_id | INT         | PK, AUTO_INCREMENT      |                       |
| car_id         | INT         | NOT NULL, FK → car      |                       |
| customer_id    | INT         | NOT NULL, FK → customer |                       |
| start_date     | DATE        | NOT NULL                | 貸出日                |
| end_date       | DATE        | NOT NULL                | 返却予定日            |
| status         | VARCHAR(10) | NOT NULL                | `RENTED` / `RETURNED` |
| created_at     | DATETIME    | NOT NULL                |                       |
| updated_at     | DATETIME    | NOT NULL                |                       |

### 設計上の論点(意図的に残すもの)

- **`car.status` と `reservation.status` の二重管理**
  車の「今の状態」と、貸出1件ごとの「ライフサイクル」を別々に持っている。両者の整合性を保つ責務は Service 層にある。返却処理で片方だけ更新するとデータが壊れるため、**トランザクション境界と Service 層の存在意義を体感するための題材**として意図的にこの設計を採用する。
- **「予約中」ステータスを持たない**
  フェーズ1では将来日付の予約を扱わず、カウンターでの貸出手続きのみを想定する。予約(顧客が事前に押さえる)概念はシステムB(フェーズ2)で追加する。

## 6. 機能要件

### 6-1. 共通

- 全画面共通のサイドバーを持つ(Thymeleaf のレイアウト機能で共通化)
  - メニュー: 「車両管理」「貸出管理」
- 登録・更新処理は **PRG パターン**(POST → リダイレクト → GET)で実装する
- 完了メッセージは Flash Scope(`RedirectAttributes.addFlashAttribute()`)で受け渡す

### 6-2. 車両一覧(CarList)

- 車両を一覧表示する
- **検索条件**(いずれも任意、未入力時は条件から除外)
  - 車種名: 部分一致
  - ステータス: 完全一致(プルダウン)
- 各行に「編集」「削除」リンクを表示する
- 「新規登録」ボタンから車両登録(CarRegist)へ遷移する

> 検索条件が任意のため、**MyBatis の動的SQL(`<if>` / `<where>`)** を使う。本アプリで動的SQLを学ぶ中心の画面。

### 6-3. 車両登録(CarRegist)

- 車種名・ナンバー・ステータスを入力して登録する
- バリデーション
  - 車種名: 必須、50文字以内
  - ナンバー: 必須、20文字以内、**既存データと重複不可**
- 登録成功後は車両一覧(CarList)へリダイレクトする

### 6-4. 車両編集(CarEdit)

- 既存車両の情報を更新する
- バリデーションは車両登録(CarRegist)と同様(ナンバー重複チェックは自レコードを除外する)
- **貸出中(`RENTED`)の車両はステータスを手動変更できない**(Service 層でチェック)
- 削除は、**貸出履歴が存在する車両は削除不可**とする

### 6-5. 貸出一覧(ReservationList)

- 貸出情報を一覧表示する。車種名・顧客名を **JOIN して同時に表示する**
- **検索条件**(いずれも任意)
  - 顧客名: 部分一致
  - ステータス: 完全一致
  - 貸出日: 範囲指定(from / to)
- ステータスが `RENTED` の行にのみ「返却」ボタンを表示する

> JOIN 結果は Entity ではなく **DTO(`ReservationDetailDto`)** で受け取る。MyBatis の `resultMap` によるマッピングを学ぶ箇所。

### 6-6. 貸出登録(ReservationRegist)

- 車両・顧客・貸出日・返却予定日を指定して貸出を登録する
- 車両プルダウンには **ステータスが `AVAILABLE` の車両のみ** を表示する
- バリデーション
  - 全項目必須
  - 貸出日 ≦ 返却予定日
- **業務ルール(Service 層)**
  - 対象車両が `AVAILABLE` でない場合はエラー(プルダウン表示後に他者が貸出した場合の考慮)
  - 登録成功時、`reservation` を INSERT し、同時に `car.status` を `RENTED` に UPDATE する(**同一トランザクション**)

### 6-7. 返却処理(貸出一覧 ReservationList からのアクション)

- 「返却」ボタン押下で以下を実行する
  - `reservation.status` を `RETURNED` に更新
  - 対象車両の `car.status` を `AVAILABLE` に更新
  - 上記2つは **同一トランザクション**(`@Transactional`)
- 処理後は貸出一覧(ReservationList)へリダイレクトし、完了メッセージを表示する

> 「登録」でも「フォーム経由の更新」でもない、**状態遷移のみのワンクリック更新**。実務の承認・完了処理でよく出るパターン。

## 7. テスト方針(JUnit)

レイヤーごとにテストの種類を使い分ける。**各層を実装したら、その場で対応するテストを書く**(後回しにしない)。

### 7-1. Mapper 層 — `@MybatisTest`

- 対象: `CarMapper`, `ReservationMapper`
- 重点: **動的SQL の分岐パターンを網羅する**
  - 検索条件なし / 車種名のみ / ステータスのみ / 両方指定
  - 日付範囲の from のみ / to のみ / 両方
- JOIN 結果が `ReservationDetailDto` に正しくマッピングされること
- テストデータは `@Sql` または H2 の初期化スクリプトで投入する

### 7-2. Service 層 — Mockito

- 対象: `CarService`, `ReservationService`
- Mapper をモック化し、**業務ルールのみを検証する**
  - 貸出登録時、車両が `AVAILABLE` でなければ例外が送出されること
  - 貸出登録時、`reservation` INSERT と `car` UPDATE の両方が呼ばれること
  - 返却処理で両テーブルが更新されること
  - 貸出履歴のある車両は削除できないこと

### 7-3. Controller 層 — `@WebMvcTest` + MockMvc

- Service をモック化し、**Web 層の振る舞いのみを検証する**
  - 正常系: 期待するビュー名が返ること、Model に必要な属性が入っていること
  - バリデーションエラー時: リダイレクトせず入力画面に戻ること
  - 登録成功時: 一覧画面へリダイレクトされること(PRG)

### 7-4. 統合テスト — `@SpringBootTest`

- 貸出登録 → 返却 の一連の流れを通しで1本だけ用意する
- トランザクションが期待通り機能していることを確認する

## 8. 実装の進め方(推奨順序)

1. プロジェクト雛形作成、DB 接続確認、テーブル作成
2. `Car` 周辺を縦に1本通す(Entity → Mapper → Service → Controller → 画面)
   - このとき車両一覧(CarList)の検索は条件なしの単純な全件取得から始める
3. `CarMapper` のテストを書く
4. 車両一覧(CarList)に検索条件を追加し、**動的SQL** に踏み込む → テストも追加
5. 車両登録(CarRegist)/ 車両編集(CarEdit)を実装 → 各層のテストを追加
6. `Reservation` 周辺を実装(JOIN・DTO・トランザクションが登場)
7. 返却処理を実装 → Service 層のテストを重点的に書く
8. サイドバーの共通レイアウト化

> 2 の段階で「1機能が全レイヤーを貫通して動く」状態を作ることを最優先する。全 Entity をまとめて作ってから Controller に進む進め方は、動くものが見えるまでが長く中断しやすいため避ける。
