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

- [ ] 2. **CarMapperTest作成**
      `src/test/java/com/example/rental/mapper/CarMapperTest.java`
      - `@MybatisTest` + `@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)`(H2等への自動差し替えを無効化し、`rental_test`に接続させる)
      - `@Autowired CarMapper carMapper`
      - `@Sql(statements = {...})` で2〜3件のcarデータを事前投入(`car_id`は既存データと衝突しない値を明示的に指定)
      - `carMapper.findAll()` の戻り値の件数と各カラム(`carId`/`carName`/`numberPlate`/`status`/`createdAt`/`updatedAt`)のマッピングをAssertJで検証(順序に依存しない比較にする)
      - `@MybatisTest`はデフォルトで`@Transactional`のため後片付けのDELETEは不要

- [ ] 3. **テスト実行・確認**
      - `CarMapperTest`を実行してGreenになることを確認
      - 既存の`CarServiceTest`/`CarListControllerTest`/`RentalCarMgmtApplicationTests`にも影響がないか確認(手順1の追加が既存テストに影響しないか)

## 検証方法

- `./mvnw -Dtest=CarMapperTest test` で単体実行しGreenを確認
- `./mvnw test` でプロジェクト全体のテストを実行し、既存テストに影響がないことを確認
