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

- [ ] 2. **carテーブルの日時カラムの定義を確認**
      - `rental` / `rental_test` の**両方**で、`created_at`に`DEFAULT CURRENT_TIMESTAMP`、`updated_at`に`DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`が付いているかを`SHOW CREATE TABLE car`で確認する
      - 付いていない場合は、INSERT/UPDATE文に日時を書くか、テーブル定義を直すかをここで判断する(以降の項目の前提になる)

- [ ] 3. **CarForm・BusinessException・選択可能ステータスを作成**
      `src/main/java/com/example/rental/form/CarForm.java`(新規) / `src/main/java/com/example/rental/exception/BusinessException.java`(新規、パッケージ新設) / `src/main/java/com/example/rental/entity/CarStatus.java`
      - `CarStatus`に`selectableValues()`を追加(AVAILABLE / MAINTENANCE のみ)
      - `BusinessException`はエラー対象の項目名(null可)とメッセージキーを持つ

### 登録(CarRegist)

- [ ] 4. **Mapper: 登録用SQL + テスト**
      `src/main/java/com/example/rental/mapper/CarMapper.java` / `src/main/resources/mapper/CarMapper.xml` / `src/test/java/com/example/rental/mapper/CarMapperTest.java`
      - `insert(Car)`(`useGeneratedKeys`で採番IDを受け取る)、`countByNumberPlate(@Param numberPlate, @Param excludeCarId)`
      - テスト: insertした内容が取得できる / ナンバー重複の件数(除外なし・自分を除外)

- [ ] 5. **Service: 登録 + テスト**
      `src/main/java/com/example/rental/service/CarService.java` / `src/test/java/com/example/rental/service/CarServiceTest.java`
      - `regist(CarForm)`
      - テスト: 重複で例外かつinsertされない / RENTEDで例外 / 正常時insertが呼ばれる

- [ ] 6. **Controller・画面・文言: 登録 + テスト**
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

### 編集(CarEdit)

- [ ] 7. **Mapper: 編集用SQL + テスト**
      `CarMapper.java` / `CarMapper.xml` / `CarMapperTest.java`
      - `findById(carId)`、`update(Car)`
      - テスト: 取得・存在しないIDでnull・更新内容の反映

- [ ] 8. **Service: 取得・更新 + テスト**
      `CarService.java` / `CarServiceTest.java`
      - `findById(carId)`、`update(carId, CarForm)`
      - テスト
        - 自分を除いた重複チェック
        - 貸出中のステータス変更で例外かつupdateされない
        - **貸出中(RENTED)の車両でも、ステータスを変えなければ車種名・ナンバーは更新できる**
        - RENTEDへの変更で例外
        - 正常時updateが呼ばれる

- [ ] 9. **Controller・画面・文言: 編集 + テスト**
      `src/main/java/com/example/rental/controller/CarEditController.java`(新規) / `src/main/resources/templates/car/edit.html`(新規) / `templates/car/list.html` / `messages.properties` / `src/test/java/com/example/rental/controller/CarEditControllerTest.java`(新規)
      - `GET /cars/{carId}/edit`(存在しなければ404) → `car/edit`、`POST /cars/{carId}/edit` → 登録と同じ分岐
      - ステータスの選択肢は登録と同じく`@ModelAttribute`メソッドで載せる
      - 画面: 既存値を初期表示、項目ごとの`th:errors`とグローバルエラーの表示欄。貸出中の車両はステータスを変更不可の表示にし、値はhiddenで送る
      - 一覧: 各行に編集リンク
      - 文言: `carEdit.*`、`common.edit`、完了メッセージ
      - テスト: 既存値つき表示 / 存在しないIDで404 / 入力エラー・業務エラーで`car/edit`に戻る(選択肢もModelに入っている) / 成功でリダイレクト+Flash

> **ここで止まってレビューを待つ**

### 削除

- [ ] 10. **Mapper: 削除用SQL + テスト**
      `CarMapper.java` / `CarMapper.xml` / `CarMapperTest.java`
      - `countReservationsByCarId(carId)`、`deleteById(carId)`
      - テスト: 貸出履歴の件数・削除の反映
      - テストで`reservation`/`customer`に行を入れるため、クラスの`@Sql`のDELETEを**`reservation` → `customer` → `car`の順**にする(外部キーの参照先を後に消す)

- [ ] 11. **Service: 削除 + テスト**
      `CarService.java` / `CarServiceTest.java`
      - `delete(carId)`
      - テスト: 貸出履歴ありで例外かつ削除されない / 履歴なしで削除が呼ばれる

- [ ] 12. **Controller・画面・文言: 削除 + テスト**
      `src/main/java/com/example/rental/controller/CarListController.java` / `templates/car/list.html` / `messages.properties` / `src/test/java/com/example/rental/controller/CarListControllerTest.java`
      - `POST /cars/{carId}/delete`: 成功も失敗も`redirect:/cars`、Flashに完了またはエラーメッセージ
      - 一覧: 各行に削除ボタン(POSTのform)
      - 文言: `common.delete`、完了・エラーメッセージ
      - テスト: 削除成功・履歴ありの両方でリダイレクトとFlashの内容

> **ここで止まってレビューを待つ**

### 仕上げ

- [ ] 13. **ドキュメントの追記**
      `CLAUDE.md` / `docs/requirements.md`
      - `CLAUDE.md`: 命名規則に削除URL(`POST /cars/{carId}/delete`、`CarListController`)を追加、パッケージ構成に`exception`を追加
      - `docs/requirements.md` 4章の対応表: 削除のURLと担当Controllerの行を追加
      - `docs/requirements.md` 6-3 / 6-4: 登録・編集画面でRENTEDを選べないこと、存在しない車両IDで404にすることを追記

- [ ] 14. **テスト実行と手動確認**
      - `./mvnw test`で全テストGreen
      - アプリ起動 → 登録(正常・入力エラー・ナンバー重複)、編集(正常・重複・自分のナンバーのまま保存)、削除(正常)を画面で確認

## 把握しておくリスク・注意点

- DBの`number_plate`にもUNIQUE制約があるため、同時登録などでServiceのチェックをすり抜けても最終的にはDBエラーで弾かれる。今回その例外はハンドリングしない
- 貸出中の車両・貸出履歴ありの削除は、step6の貸出登録ができるまで画面からは作れない。Service/Mapperのテストで確認し、手動確認は対象外
- 削除時、外部キー制約でもDBエラーになるが、先にServiceでチェックするため通常は到達しない

## 検証方法

- 各項目の完了ごとに `./mvnw test` で全テストGreenを確認
- 最後にアプリを起動して、登録・編集・削除を画面で確認
