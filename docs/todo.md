# TODO: 8章 step2「Carを縦に1本通す」

`docs/requirements.md` 8章の実装順序のうち、**step2**(Car周辺を縦に1本通す)のみを対象にしたTODOリスト。
まだ実装はしていない。1項目ずつ実装し、完了したらチェックを付けていく。

## 対象範囲

- 対象: **車両一覧(CarList)のみ**、検索条件なしの単純な全件取得
- 対象外(後続stepへ明示的に先送り):
  - CarMapperのテスト → step3
  - 検索条件・動的SQL → step4
  - 車両登録(CarRegist)/ 車両編集(CarEdit) → step5
  - 共通サイドバーレイアウト → step8

## 前提・確認済み事項

- `car`テーブルはDBeaverで作成済み(MySQL `rental`スキーマ)。DDLファイル追加は不要
- テスト方針: Service(Mockito)・Controller(`@WebMvcTest`)はstep2内で書く。Mapperのテストのみstep3に委ねる
- テンプレート範囲: 検索フォーム・編集/削除リンク・新規登録ボタンは含めない最小構成(まだ存在しない車両登録(CarRegist)/車両編集(CarEdit)へのダミーリンクを作らないため)
- Serviceのメソッド名`findAll()`は要件書に明記がないための想定。異論があれば`list()`等に変更可

## TODOリスト

- [x] 1. **MyBatis設定を追加**(既存ファイル変更)
     `src/main/resources/application.properties`
     - `mybatis.configuration.map-underscore-to-camel-case=true` を追加(スネークケース列↔キャメルケースプロパティの自動マッピングのため)

- [x] 2. **Mapperの登録方式を決定**
     - 当初は`config/MyBatisConfig.java`に`@MapperScan`を切り出していたが、step3で`@MybatisTest`のスライスからも除外されBeanが解決できないことが判明
     - 最終的に**Mapperインターフェースに`@Mapper`を付ける方式**に変更し、`MyBatisConfig`は削除(MyBatisの自動設定が`@Mapper`を検出するため、アプリ実行時・`@MybatisTest`の双方で動作する)

- [x] 3. **Entity作成**
     `src/main/java/com/example/rental/entity/Car.java`
     - `carId(Integer)` / `carName(String)` / `numberPlate(String)` / `status(String)` / `createdAt(LocalDateTime)` / `updatedAt(LocalDateTime)`
     - Lombokの`@Data`を付け、getter/setterは手書きしない

- [x] 4. **Mapper interface作成**
     `src/main/java/com/example/rental/mapper/CarMapper.java`
     - `@Mapper`を付け、`List<Car> findAll();` のみ(条件なし全件取得)

- [x] 5. **Mapper XML作成**
     `src/main/resources/mapper/CarMapper.xml`
     - namespace: `com.example.rental.mapper.CarMapper`
     - `findAll` は単純な `SELECT ... FROM car`(`<if>`/`<where>`は使わない)

- [x] 6. **Service作成**
     `src/main/java/com/example/rental/service/CarService.java`
     - `@Service` + `@RequiredArgsConstructor`、`private final CarMapper`でコンストラクタインジェクション
     - `findAll()`が`carMapper.findAll()`をそのまま返す

- [x] 7. **Serviceテスト作成**
     `src/test/java/com/example/rental/service/CarServiceTest.java`
     - Mockitoで`CarMapper`をモック化し、`findAll()`がモックの戻り値をそのまま返すことのみ検証

- [x] 8. **Controller作成**
     `src/main/java/com/example/rental/controller/CarListController.java`
     - `@Controller`、`@GetMapping("/cars")`
     - `@RequiredArgsConstructor`で`CarService`をコンストラクタインジェクション、結果をModel属性(`carList`)に詰めてビュー名`"car/list"`を返す

- [x] 9. **Controllerテスト作成**
     `src/test/java/com/example/rental/controller/CarListControllerTest.java`
     - `@WebMvcTest(CarListController.class)` + `@MockitoBean CarService`
     - 検証: ステータス200、ビュー名`car/list`、Model属性`carList`の存在

- [x] 10. **messages.properties作成**
      `src/main/resources/messages.properties`
      - `carList.title`(車両一覧) / `carList.carId` / `carList.carName` / `carList.numberPlate` / `carList.status`
      - 検索・新規登録・編集・削除関連のキーはstep4/5まで作らない

- [x] 11. **テンプレート作成**
      `src/main/resources/templates/car/list.html`
      - 最小構成: タイトル + 一覧テーブルのみ
      - `th:each`でModel属性`carList`をループし、`carId`/`carName`/`numberPlate`/`status`を表示。見出しは`th:text="#{carList.xxx}"`で参照

- [ ] 12. **手動疎通確認**
      - アプリ起動→ `http://localhost:8080/cars` にアクセスし、実DBの`car`テーブルの内容が一覧表示されることを目視確認

## 検証方法

- 単体テスト: `CarServiceTest` / `CarListControllerTest` を実行しGreenになることを確認
- 手動確認: アプリ起動後 `/cars` にアクセスし、DBの`car`テーブルの内容が一覧表示されることを確認(TODO 12)

---

# TODO: 8章 step3「CarMapperのテスト追加」

`docs/requirements.md` 8章の実装順序のうち、**step3**(CarMapperのテスト追加)のみを対象にしたTODOリスト。
まだ実装はしていない。1項目ずつ実装し、完了したらチェックを付けていく。

## 対象範囲

- 対象: `CarMapper`(現状`findAll()`のみ、動的SQL・JOINはまだ無い)の`@MybatisTest`
- 7-1の記載通り**Mapperの動作検証**(DBの内容が`Car`エンティティに正しくマッピングされて返ること)に絞る
- 対象外(後続stepへ明示的に先送り):
  - 動的SQLの分岐パターンのテスト → step4(検索条件追加とセット)
  - 業務ルールの検証(Service層の責務) → 7-2の範囲であり本stepでは扱わない

## 前提・確認済み事項

- `rental_test`スキーマ・`car`テーブルはDBeaverで作成済み
- テストデータ投入方法: `@Sql(statements = ...)`をテストクラス内に直接記述(別ファイル化はしない)
- H2は使わない(pom.xmlに依存なし、CLAUDE.mdの方針通りMySQL/`rental_test`のみ)

## TODOリスト

- [x] 1. **テスト用DB接続設定を追加**(新規ファイル)
     `src/test/resources/application.properties`
     - `spring.datasource.url=jdbc:mysql://localhost:3306/rental_test`(username/password/driver-class-nameは本番と同じ値)
     - `mybatis.mapper-locations=classpath:mapper/*.xml`
     - `mybatis.configuration.map-underscore-to-camel-case=true`
     - ※テスト用クラスパスの`application.properties`は本番設定を完全に置き換える(マージされない)ため、必要な設定を漏れなく複製する

- [x] 2. **CarMapperTest作成**
     `src/test/java/com/example/rental/mapper/CarMapperTest.java`
     - `@MybatisTest` + `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)`(H2等への自動差し替えを無効化し、`rental_test`に接続させる)
     - `@Autowired CarMapper carMapper`
     - `@Sql(statements = {...})` で2件のcarデータを事前投入(`car_id`は既存データと衝突しない値を明示的に指定)。先頭に`DELETE FROM car`を入れてテーブルの状態に依存しないようにする
     - `carMapper.findAll()` の戻り値の各カラム(`carId`/`carName`/`numberPlate`/`status`/`createdAt`/`updatedAt`)のマッピングをAssertJで検証(`containsExactlyInAnyOrder`で順序に依存しない比較)
     - 0件の場合に空リストを返すことも検証
     - `@MybatisTest`はデフォルトで`@Transactional`のため後片付けのDELETEは不要
     - ※`@MapperScan`を`MyBatisConfig`に切り出していたため`@MybatisTest`のスライスから除外されBeanが見つからなかった。`CarMapper`に`@Mapper`を付けて`MyBatisConfig`を削除する方式に変更(実装中に判明)

- [x] 3. **テスト実行・確認**
     - `CarMapperTest`を実行してGreenになることを確認
     - 既存の`CarServiceTest`/`CarListControllerTest`/`RentalCarMgmtApplicationTests`にも影響がないか確認(手順1の追加が既存テストに影響しないか)

## 検証方法

- `./mvnw -Dtest=CarMapperTest test` で単体実行しGreenを確認
- `./mvnw test` でプロジェクト全体のテストを実行し、既存テストに影響がないことを確認

---

# TODO: 8章 step4「車両一覧に検索条件を追加し、動的SQLに踏み込む」

`docs/requirements.md` 8章の実装順序のうち、**step4**(車両一覧に検索条件を追加し、動的SQLに踏み込む → テストも追加)のみを対象にしたTODOリスト。
まだ実装はしていない。1項目ずつ実装し、完了したらチェックを付けていく。

## 対象範囲

- 対象: 要件書6-2の検索条件(いずれも任意、未入力時は条件から除外)
  - 車種名: 部分一致
  - ステータス: 完全一致(プルダウン)
- MyBatisの`<where>`/`<if>`による動的SQLと、7-1の分岐パターン(条件なし / 車種名のみ / ステータスのみ / 両方指定)を網羅するMapperテスト
- 対象外(後続stepへ明示的に先送り):
  - 各行の編集/削除リンク、新規登録ボタン → step5
  - 共通サイドバーレイアウト → step8

## 前提・確認済み事項

- Mapperには`CarSearchForm`をそのまま渡す: `List<Car> search(CarSearchForm form)`
- `CarStatus` enum(`AVAILABLE` / `RENTED` / `MAINTENANCE`)を`entity`パッケージに新規作成し、**`Car.status`と`CarSearchForm.status`の型もenumにする**
- ステータスは画面上コード値のまま表示する(日本語ラベル化はしない)
- 検索条件は任意のため、バリデーション(`spring-boot-starter-validation`)は導入しない

## TODOリスト(各項目の完了時点で全テストGreenを保てる順序)

- [x] 1. **CarStatus enum作成と`Car.status`の型変更**
     `src/main/java/com/example/rental/entity/CarStatus.java`(新規) / `src/main/java/com/example/rental/entity/Car.java`
     - `Car.status`を`String`から`CarStatus`に変更
     - 既存テスト3件を追従: `CarMapperTest`(期待値を`CarStatus.AVAILABLE`等に)、`CarServiceTest`・`CarListControllerTest`(`setStatus(CarStatus.AVAILABLE)`)
     - MyBatisは標準の`EnumTypeHandler`がVARCHAR↔enum名を変換するため設定追加は不要(テスト実行で確認)
     - ※Entityの型変更のため、既存テストの修正も同じコミットに含めないとコンパイルが通らない

- [x] 2. **CarSearchForm作成**
     `src/main/java/com/example/rental/form/CarSearchForm.java`(新規、`form`パッケージ新設)
     - `@Data`、フィールド `carName`(String) / `status`(CarStatus)

- [x] 3. **Mapperに動的SQLの`search`を追加 + Mapperテスト**
     `src/main/java/com/example/rental/mapper/CarMapper.java` / `src/main/resources/mapper/CarMapper.xml` / `src/test/java/com/example/rental/mapper/CarMapperTest.java`
     - `List<Car> search(CarSearchForm form);` を追加(この時点では`findAll`も残し、Service/Controllerを壊さない)
     - XMLは`<where>`の中に以下を置く
       - `<if test="carName != null and carName != ''">AND car_name LIKE CONCAT('%', #{carName}, '%')</if>`
       - `<if test="status != null">AND status = #{status}</if>`
     - `ORDER BY car_id` を付けて一覧の並びを安定させる
     - `@Sql`で車種名・ステータスが異なる3件程度を投入し、以下を検証
       - 条件なし(全件) / 車種名のみ(部分一致) / ステータスのみ / 両方指定 / 車種名が空文字(条件から除外される) / 該当0件

- [x] 4. **Serviceを`search`に切り替え + テスト**
     `src/main/java/com/example/rental/service/CarService.java` / `src/test/java/com/example/rental/service/CarServiceTest.java`
     - `findAll()` → `search(CarSearchForm form)`(Mapperへ委譲)
     - 受け取ったFormをそのままMapperに渡し、結果を返すことを検証
     - ※Controllerが`findAll()`を呼んでいるため、この項目ではControllerの呼び出しも`search(new CarSearchForm())`等へ最小限追従させる(本格対応は項目5)

- [x] 5. **Controller・画面・文言を検索対応 + テスト**
     `src/main/java/com/example/rental/controller/CarListController.java` / `src/main/resources/templates/car/list.html` / `src/main/resources/messages.properties` / `src/test/java/com/example/rental/controller/CarListControllerTest.java`
     - Controller: `list(@ModelAttribute CarSearchForm carSearchForm, Model model)`
       - `carList`に`carService.search(carSearchForm)`の結果、`statusList`に`CarStatus.values()`を詰める
     - テンプレート: 一覧テーブルの上にGETの検索フォームを追加
       - `th:object="${carSearchForm}"`、車種名は`th:field="*{carName}"`のテキスト、ステータスは`th:field="*{status}"`の`<select>`(先頭に未選択の空option、選択肢は`statusList`)、検索ボタン
     - messages.properties: `carList.searchCarName` / `carList.searchStatus` / `carList.statusUnselected`(未選択の表示) / `common.search`(検索ボタン。貸出一覧でも使うため`common.`)
     - Controllerテスト
       - 条件なしのGETで、ビュー名・`carList`・`statusList`・`carSearchForm`がModelにあること
       - `?carName=プリ&status=RENTED`のGETで、Serviceに渡ったFormに値がバインドされていること(`ArgumentCaptor`で検証)

- [x] 6. **不要になった`findAll`を削除**
     `src/main/java/com/example/rental/mapper/CarMapper.java` / `src/main/resources/mapper/CarMapper.xml` / `src/test/java/com/example/rental/mapper/CarMapperTest.java`
     - Mapperの`findAll`とそのテストを削除(項目3の「条件なし」「0件」ケースで代替済み)

- [x] 7. **テスト実行と手動確認**
     - `./mvnw test` で全テストGreen
     - アプリ起動 → `http://localhost:8080/cars` で、未入力・車種名のみ・ステータスのみ・両方の検索が期待通り動くことを目視確認

## 把握しておくリスク・注意点

- `status=`(空文字)はSpringのenum変換で`null`になり条件から除外される。一方`status=FOO`のような不正値はバインドエラー(400)になる。プルダウン経由では発生しないため、step4ではハンドリングしない
- `LIKE`の部分一致で、入力に`%`や`_`が含まれるとワイルドカードとして解釈される。学習範囲外として今回はエスケープしない

## 検証方法

- 各項目の完了ごとに `./mvnw test` で全テストGreenを確認
- 最後にアプリを起動し `/cars` で検索動作を目視確認

---

# TODO: 8章 step5「車両登録 / 車両編集を実装 → 各層のテストを追加」

`docs/requirements.md` 8章の実装順序のうち、**step5**(車両登録 / 車両編集を実装 → 各層のテストを追加)のみを対象にしたTODOリスト。
まだ実装はしていない。1項目ずつ実装し、完了したらチェックを付けていく。

**登録・編集・削除の3つの区切りでいったん止まり、人間側のレビューを待ってから次に進む。**

## 対象範囲

- 対象: 要件書6-3(車両登録)、6-4(車両編集・削除)、6-2で先送りしていた一覧の「新規登録ボタン」「編集/削除リンク」
- 初めてバリデーション・PRG・Flashメッセージ・Service層の業務ルールが登場する
- 対象外(後続stepへ明示的に先送り):
  - 貸出(Reservation)周り → step6
  - 共通サイドバーレイアウト → step8

## 前提・確認済み事項

- **削除もstep5で実装**する(貸出履歴があれば削除不可)。`reservation`テーブルはDBに作成済み(0件)なので、`CarMapper`に貸出件数を数えるSQLを足して判定する
- **業務エラーはServiceが独自例外(`BusinessException`)を投げ、Controllerが捕まえる**
  - 項目名(`field`)がある場合: `bindingResult.rejectValue(field, messageKey)`で入力欄のエラーにする
  - `field`が`null`の場合: `bindingResult.reject(messageKey)`でグローバルエラーにする。テンプレートにはグローバルエラーの表示欄(`th:errors="*{global}"`)を置く
  - 削除のエラー: 一覧へリダイレクトし、Flashでエラーメッセージを表示
- **RENTEDは登録・編集画面で選べない**。選択肢は AVAILABLE / MAINTENANCE のみ。貸出中の車両を編集するときは、ステータスを変更できない表示にする
- **ステータスの選択肢は`@ModelAttribute`メソッドでModelに載せる**。毎回明示的に詰めると、バリデーションエラーで入力画面に戻すときに詰め忘れて画面が壊れるため

## 要件書・CLAUDE.mdから外れる判断

- **`exception`パッケージを新設**する(`BusinessException`の置き場)。CLAUDE.mdのパッケージ構成に無いが、Service/Controllerどちらにも属さない共通の型のため
- **削除のURL `POST /cars/{carId}/delete` を追加し、`CarListController`に置く**。CLAUDE.mdの命名規則に削除URLの記載が無いため追加になる。置き場所は、返却処理を「専用画面を持たない一覧からのPOSTアクション」として`ReservationListController`に置く既存ルールと揃えた(削除も専用画面を持たず、一覧の各行から実行するため)。データを変える操作なのでGETリンクではなくPOSTのボタンにする
- **存在しない`carId`で編集画面を開いたら404**にする(要件書に記載なし)

## 設計の要点

- `CarForm`(登録・編集で共用): `carName` `@NotBlank @Size(max=50)` / `numberPlate` `@NotBlank @Size(max=20)` / `status` `@NotNull`
- バリデーションメッセージは`messages.properties`に置く。Springのメッセージコード規約(例: `NotBlank.carForm.carName=車種名を入力してください`)を使い、アノテーション側には文言を書かない
- 登録・更新はServiceが`CarForm`を`Car`エンティティに詰め替えてMapperに渡す(検索は条件なのでFormのまま、登録・更新は保存データなのでEntity)
- `created_at` / `updated_at` はDBのDEFAULT / ON UPDATEに任せ、INSERT/UPDATE文には書かない(項目2で両スキーマの定義を確認する)
- ナンバー重複チェックは`countByNumberPlate(numberPlate, excludeCarId)`の1本。`excludeCarId`が指定されたときだけ`<if>`で自レコードを除外(登録はnull、編集は自分のID)
- 手動で選べるステータスは`CarStatus`にメソッド(例: `selectableValues()`)を用意し、登録・編集Controllerで共用
- 業務ルール(Service)
  - 登録: ナンバー重複→例外 / ステータスがRENTED→例外
  - 更新: 対象が無い→例外 / ナンバー重複(自分を除く)→例外 / 現在RENTEDなのにステータスを変える→例外 / RENTED以外からRENTEDにする→例外
  - 削除: 貸出履歴あり→例外(削除SQLは呼ばない)

## TODOリスト(各項目の完了時点で全テストGreenを保てる順序)

### 共通の準備

- [x] 1. **バリデーション依存を追加**
     `pom.xml`
     - `spring-boot-starter-validation`を追加し、全テストが通ることを確認

- [x] 2. **carテーブルの日時カラムの定義を確認**
     - `rental` / `rental_test` の**両方**で、`created_at`に`DEFAULT CURRENT_TIMESTAMP`、`updated_at`に`DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`が付いているかを`SHOW CREATE TABLE car`で確認する
     - 付いていない場合は、INSERT/UPDATE文に日時を書くか、テーブル定義を直すかをここで判断する(以降の項目の前提になる)

- [x] 3. **CarForm・BusinessException・選択可能ステータスを作成**
     `src/main/java/com/example/rental/form/CarForm.java`(新規) / `src/main/java/com/example/rental/exception/BusinessException.java`(新規、パッケージ新設) / `src/main/java/com/example/rental/entity/CarStatus.java`
     - `CarStatus`に`selectableValues()`を追加(AVAILABLE / MAINTENANCE のみ)
     - `BusinessException`はエラー対象の項目名(null可)とメッセージキーを持つ

### 登録(CarRegist)

- [x] 4. **Mapper: 登録用SQL + テスト**
     `src/main/java/com/example/rental/mapper/CarMapper.java` / `src/main/resources/mapper/CarMapper.xml` / `src/test/java/com/example/rental/mapper/CarMapperTest.java`
     - `insert(Car)`(`useGeneratedKeys`で採番IDを受け取る)、`countByNumberPlate(@Param numberPlate, @Param excludeCarId)`
     - テスト: insertした内容が取得できる / ナンバー重複の件数(除外なし・自分を除外)

- [x] 5. **Service: 登録 + テスト**
     `src/main/java/com/example/rental/service/CarService.java` / `src/test/java/com/example/rental/service/CarServiceTest.java`
     - `regist(CarForm)`
     - テスト: 重複で例外かつinsertされない / RENTEDで例外 / 正常時insertが呼ばれる

- [x] 6. **Controller・画面・文言: 登録 + テスト**
     `src/main/java/com/example/rental/controller/CarRegistController.java`(新規) / `src/main/resources/templates/car/regist.html`(新規) / `src/main/resources/templates/car/list.html` / `src/main/resources/messages.properties` / `src/test/java/com/example/rental/controller/CarRegistControllerTest.java`(新規)
     - `GET /cars/new` → `car/regist`、`POST /cars/new` → バリデーションエラー・業務エラーは`car/regist`に戻す、成功は`redirect:/cars`+Flash完了メッセージ
     - ステータスの選択肢は`@ModelAttribute("statusList")`メソッドで載せる
     - 画面: 車種名・ナンバー・ステータスの入力、項目ごとの`th:errors`、グローバルエラーの表示欄
     - 一覧: 新規登録ボタンとFlashメッセージの表示欄を追加
     - 文言: `carRegist.*`、`common.regist`、バリデーションメッセージ、業務エラーメッセージ、完了メッセージ
     - テスト
       - 画面表示 / 選択肢にRENTEDが含まれない
       - 入力エラーで`car/regist`に戻る(リダイレクトしない)。**このとき選択肢もModelに入っている**
       - 業務エラーで`car/regist`に戻る。**このとき選択肢もModelに入っている**
       - 成功で`/cars`へリダイレクトしFlashに完了メッセージ

> **ここで止まってレビューを待つ**

# TODO: 8章 step5(組み直し版)

登録(項目1〜6)は完了済みのため、そのまま残す。
**編集・削除は「画面から見える小さな単位で、全レイヤーを縦に1本ずつ通す」順番に組み直す。**

## 組み直しの理由

- 旧TODOは Mapper → Service → Controller の「下から積む」順番だった。作業としては安全だが、部品が何に使われるのか最後まで見えず、学習には向かなかった
- 今後は、各項目の完了時点で **画面かテストで動きが見える** ことを優先する
- 各項目で「全テストGreen」は引き続き守る。テストはその項目の中で書く(後回しにしない)

## 進め方のルール(学習モード)

- 各項目は **ヒント → 自分で実装 → レビュー → 説明できるか確認** の順で進める
- Claude Code は実装コードを書かない。ヒントとレビューのみ(本人が「答えを見せて」と言ったときだけコードを示す)
- 各項目の「確認ポイント」に自分の言葉で答えられたら、チェックを付けて次へ進む

---

## 0. 登録の理解を固める(新規コードなし)

- [ ] 0-a. **登録を動かして観察する** - 正常登録 / 車種名を空で送信 / 既存ナンバーで送信 / (curl等で)status=RENTED を送信 - それぞれ画面に何が出たかをメモする
- [ ] 0-b. **1回の登録リクエストを追いかける** - ボタン押下 → `CarRegistController` → `CarService.regist` → `CarMapper.insert` → リダイレクト → 一覧、を実際のファイルを開きながら順に追う - ナンバー重複のときに、`BusinessException` がどこで投げられ、どこで捕まり、どの行で画面の赤字になるかを特定する
- [ ] 0-c. **壊して観察する**(1つずつ試して元に戻す) - `@ModelAttribute("statusList")` を消す → 入力エラー時のプルダウンはどうなるか - `catch (BusinessException e)` を消す → ナンバー重複時の画面はどうなるか - `regist.html` の `th:errors` を1つ消す → 何が起きるか - 実行前に結果を予想し、予想と実際の差をメモする

確認ポイント:

- `BusinessException` を投げてから画面に赤字が出るまでに通るファイルを、順番に3つ以上言える
- `rejectValue` と `reject` の違いを一言で言える
- 入力チェック(`@NotBlank` 等)と業務チェック(`BusinessException`)が、それぞれどの層で行われているか言える

> **ここで止まってレビューを待つ**

---

## 編集(CarEdit)

- [ ] 7. **編集画面を開くだけ(値はまだ出さない)**
     `CarEditController.java`(新規) / `templates/car/edit.html`(新規) / `templates/car/list.html` / `messages.properties` / `CarEditControllerTest.java`(新規)
     - `GET /cars/{carId}/edit` → `car/edit` を返すだけ。画面にはタイトルと、受け取った `carId` を表示する
     - 一覧の各行に編集リンクを追加
     - テスト: ビュー名が `car/edit` になる
       確認ポイント: URLの `{carId}` が、どうやってControllerの引数に届いたか

- [ ] 8. **既存の値を表示する(1台取得を縦に通す)**
     `CarMapper.java/.xml` / `CarMapperTest.java` / `CarService.java` / `CarServiceTest.java` / `CarEditController.java` / `edit.html` / `CarEditControllerTest.java`
     - Mapper `findById(carId)`、Service `findById(carId)`
     - Controller で `Car` を `CarForm` に詰め替えて Model に入れ、入力欄に初期表示する
     - テスト: Mapper(取得できる / 存在しないIDで null)、Controller(Modelに既存値が入っている)
       確認ポイント: 登録では空の `CarForm` を渡していたのに、編集では値入りの `CarForm` を渡す理由

- [ ] 9. **存在しない車両IDで404にする**
     `CarEditController.java` / `CarEditControllerTest.java`
     - `findById` が null なら404
     - テスト: 存在しないIDで404
       確認ポイント: 404にせず放っておくと何が起きるか

- [ ] 10. **保存する(チェックなし)**
      `CarMapper.java/.xml` / `CarMapperTest.java` / `CarService.java` / `CarServiceTest.java` / `CarEditController.java` / `CarEditControllerTest.java` / `messages.properties`
      - Mapper `update(Car)`、Service `update(carId, CarForm)`(詰め替えてupdateを呼ぶだけ)
      - `POST /cars/{carId}/edit` → `redirect:/cars` + Flash完了メッセージ
      - テスト: Mapper(更新内容が反映される)、Service(updateが呼ばれる)、Controller(リダイレクトとFlash)
        確認ポイント: 登録の `insert` と比べて、SQLとJavaで何が違うか

- [ ] 11. **入力チェックを付ける**
      `CarEditController.java` / `edit.html` / `CarEditControllerTest.java`
      - 登録と同じ `@Validated` + `BindingResult`。エラー時は `car/edit` に戻す
      - ステータスの選択肢は `@ModelAttribute("statusList")` で載せる
      - テスト: 入力エラーで `car/edit` に戻る(リダイレクトしない)、選択肢がModelに入っている
        確認ポイント: 登録のコードを見ずに書けたか。見た場合、どこを見たか

- [ ] 12. **ナンバー重複チェック(自分を除く)**
      `CarService.java` / `CarServiceTest.java` / `CarEditController.java` / `CarEditControllerTest.java`
      - Service で `countByNumberPlate(numberPlate, carId)` を使う
      - Controller で `BusinessException` を捕まえて `car/edit` に戻す
      - テスト: Service(他の車と重複で例外かつupdateされない / 自分のナンバーのままなら更新できる)、Controller(業務エラーで `car/edit` に戻る)
        確認ポイント: 登録では第2引数に null を渡した。編集で carId を渡す理由

- [ ] 13. **貸出中(RENTED)のルール**
      `CarService.java` / `CarServiceTest.java` / `edit.html` / `messages.properties`
      - Service: 現在RENTEDなのにステータスを変える→例外 / RENTED以外からRENTEDにする→例外
      - 画面: 貸出中の車両はステータスをプルダウンではなく文字で表示し、値は hidden で送る
      - テスト(Service)
        - 貸出中のステータス変更で例外かつupdateされない
        - RENTEDへの変更で例外
        - **貸出中でも、ステータスを変えなければ車種名・ナンバーは更新できる**
      - 手動確認: DBeaverで対象車両の status を `RENTED` に書き換えてから編集画面を開く
        確認ポイント: hidden にせず `disabled` にすると何が起きるか

- [ ] 14. **更新対象が見つからないとき(グローバルエラー)**
      `CarService.java` / `CarServiceTest.java` / `CarEditController.java` / `edit.html`
      - Service: 更新前に対象が無ければ `BusinessException(messageKey)`(field なし)
      - Controller: field が null なら `reject`。画面にグローバルエラーの表示欄
      - テスト: Service(対象なしで例外)
        確認ポイント: この場合に `rejectValue` を使うと何が起きるか

> **ここで止まってレビューを待つ**

---

## 削除

- [ ] 15. **削除ボタンを押すと一覧に戻る(削除はまだしない)**
      `CarListController.java` / `list.html` / `messages.properties` / `CarListControllerTest.java`
      - 各行に削除ボタン(POSTの form)。`POST /cars/{carId}/delete` → `redirect:/cars`
      - テスト: リダイレクトされる
        確認ポイント: 削除をリンク(GET)ではなくボタン(POST)にする理由

- [ ] 16. **実際に削除する**
      `CarMapper.java/.xml` / `CarMapperTest.java` / `CarService.java` / `CarServiceTest.java` / `CarListController.java` / `CarListControllerTest.java`
      - Mapper `deleteById(carId)`、Service `delete(carId)`、Flashに完了メッセージ
      - テスト: Mapper(削除が反映される)、Service(deleteが呼ばれる)、Controller(Flashの内容)
        確認ポイント: 登録・編集と違い、削除は `BindingResult` を使わない理由

- [ ] 17. **貸出履歴があれば削除できない**
      `CarMapper.java/.xml` / `CarMapperTest.java` / `CarService.java` / `CarServiceTest.java` / `CarListController.java` / `CarListControllerTest.java`
      - Mapper `countReservationsByCarId(carId)`
      - Service: 1件以上なら例外(deleteは呼ばない)
      - Controller: 例外を捕まえて、Flashでエラーメッセージを渡して一覧へ
      - `CarMapperTest` の `@Sql` の DELETE を `reservation` → `customer` → `car` の順にする
      - テスト: Mapper(件数)、Service(履歴ありで例外かつ削除されない)、Controller(エラー時のFlash)
      - 手動確認: DBeaverで customer と reservation に1行ずつ入れてから削除を試す
        確認ポイント: 入力画面が無い削除で、エラーをどうやって利用者に見せているか

> **ここで止まってレビューを待つ**

---

## 仕上げ

- [ ] 18. **ドキュメントの追記**
      - `CLAUDE.md`: 削除URL(`POST /cars/{carId}/delete`、`CarListController`)、パッケージ構成に `exception`、業務エラーのメッセージキーは `フォーム名.項目名.エラー内容`
      - `docs/requirements.md` 4章の対応表に削除の行、6-3/6-4 に「RENTEDは手動で選べない」「存在しない車両IDは404」

- [ ] 19. **全体確認**
      - `./mvnw test` で全テストGreen
      - 登録・編集・削除を画面で一通り確認

## 把握しておくリスク・注意点

- DBの `number_plate` のUNIQUE制約違反(同時登録など)は今回ハンドリングしない
- 存在しないIDでの削除は0件削除になるだけで、今回は扱わない
