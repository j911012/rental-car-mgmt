# プロジェクト概要

レンタカー管理システム(業者側・システムA)。Spring Boot + MyBatis + JUnit の学習用アプリ。
詳細な機能要件・画面仕様・テーブル定義は `docs/requirements.md` を参照。実装を始める前に必ず対応する章を確認すること。

## 技術スタック

- Java 17 / Spring Boot 4.0.8 / Maven
- MyBatis(`mybatis-spring-boot-starter`) — JPAは使わない
- Lombok — Entity / Form / DTO には `@Data` を付け、getter/setterは手書きしない
- Thymeleaf
- DB: MySQL(DBeaverで接続確認する。テストは別スキーマ `rental_test` を使う)
- テスト: JUnit 5 / Mockito / AssertJ / `mybatis-spring-boot-starter-test`

## パッケージ構成(package by layer)

petclinicのようなpackage by featureではなく、レイヤーごとに分ける。

```
com.example.rental
├── controller
├── service
├── mapper
├── entity
├── dto      // JOIN結果など、Entityで表現できないもの
└── form     // 画面入力・検索条件用
```

## 命名規則

画面ID(`C1000List`のような記号的な命名)は不採用。**エンティティ名(Car / Reservation)ベースの、意味の分かる名前**に統一する。

- URL: `/cars`(一覧), `/cars/new`(登録), `/cars/{carId}/edit`(編集), `/reservations`(一覧), `/reservations/new`(登録)
- Controllerクラス名: `CarListController`, `CarRegistController`, `CarEditController`, `ReservationListController`, `ReservationRegistController`
- テンプレート: `templates/car/list.html`, `templates/car/regist.html`, `templates/car/edit.html`, `templates/reservation/list.html`, `templates/reservation/regist.html`
- 返却処理は専用画面を持たず、`ReservationListController`内のPOSTアクション(`/reservations/{reservationId}/return`)として実装する

## 実装時のルール

- **登録・更新はPRGパターン**(POST→リダイレクト→GET)で実装する。POST後にそのまま画面を返さない
- 完了メッセージは `RedirectAttributes.addFlashAttribute()` で渡す
- 複数テーブルを更新する処理(貸出登録・返却処理など)には `@Transactional` を付ける
- 検索条件が任意項目の場合は、MyBatisの `<if>` / `<where>` タグで動的SQLを組む
- JOIN結果はEntityに無理に詰めず、DTOを作る
- Entity / Form / DTO は Lombok の `@Data` を使い、getter/setter/toString/equals/hashCode は手書きしない
- Controller / Service などDIを受けるクラスは、Lombok の `@RequiredArgsConstructor` + `private final` フィールドでコンストラクタインジェクションする(コンストラクタは手書きしない)

## 画面文言の管理(i18n)

画面に表示するタイトル・ラベル・ボタン文言は、HTMLやJavaに直書きせず `src/main/resources/messages.properties` に集約する。

- 英語版は作らない。多言語切り替え(`LocaleResolver`)の実装は不要
- キー名は「画面名(camelCase).項目名」を基本形にする(例: `carList.title`, `carRegist.submit`)
- ボタン名など画面をまたいで共通のものは `common.` プレフィックスを使う(例: `common.regist`, `common.edit`, `common.delete`)
- Thymeleaf側では `th:text="#{キー名}"` で参照する(HTMLタグを含む文言のみ `th:utext`)
- `messages.properties` はエディタ上で日本語のまま編集してよい(保存時にIDEが自動でUnicodeエスケープすることがあるが、動作に影響はないため気にしなくてよい)

## テストのルール

- **実装したら、同じタイミングで対応するテストも書く**(後回しにしない)
- Mapper層 → `@MybatisTest`
- Service層 → Mockitoでモック化し、業務ルールのみ検証
- Controller層 → `@WebMvcTest` + MockMvc
- 統合テストは最小限(貸出→返却の一連の流れを1本のみ)

## 進め方(重要)

- 新しい機能に着手する前に、**まずTODOリストへの分解だけを提示し、実装はまだしない**。人間側のレビューを待つ
- 1回で複数の画面・機能をまとめて実装しない。要件書8章のステップ単位で1つずつ進める
- 実装が要件書の内容と異なる設計判断をする場合(層の省略、命名規則からの逸脱など)は、実装前に理由を説明する
