# TODO: 8章 step2「Carを縦に1本通す」

`docs/requirements.md` 8章の実装順序のうち、**step2**(Car周辺を縦に1本通す)のみを対象にしたTODOリスト。
まだ実装はしていない。1項目ずつ実装し、完了したらチェックを付けていく。

## 対象範囲

- 対象: **C1000List(車両一覧)のみ**、検索条件なしの単純な全件取得
- 対象外(後続stepへ明示的に先送り):
  - CarMapperのテスト → step3
  - 検索条件・動的SQL → step4
  - C1000Regist / C1000Edit → step5
  - 共通サイドバーレイアウト → step8

## 前提・確認済み事項

- `car`テーブルはDBeaverで作成済み(MySQL `rental`スキーマ)。DDLファイル追加は不要
- テスト方針: Service(Mockito)・Controller(`@WebMvcTest`)はstep2内で書く。Mapperのテストのみstep3に委ねる
- テンプレート範囲: 検索フォーム・編集/削除リンク・新規登録ボタンは含めない最小構成(まだ存在しないC1000Regist/C1000Editへのダミーリンクを作らないため)
- Serviceのメソッド名`findAll()`は要件書に明記がないための想定。異論があれば`list()`等に変更可

## TODOリスト

- [x] 1. **MyBatis設定を追加**(既存ファイル変更)
      `src/main/resources/application.properties`
      - `mybatis.configuration.map-underscore-to-camel-case=true` を追加(スネークケース列↔キャメルケースプロパティの自動マッピングのため)

- [x] 2. **Mapperスキャン設定を追加**(既存ファイル変更)
      `src/main/java/com/example/rental/RentalCarMgmtApplication.java`
      - `@MapperScan("com.example.rental.mapper")` を追加

- [x] 3. **Entity作成**
      `src/main/java/com/example/rental/entity/Car.java`
      - `carId(Integer)` / `carName(String)` / `numberPlate(String)` / `status(String)` / `createdAt(LocalDateTime)` / `updatedAt(LocalDateTime)`
      - Lombok未導入のためgetter/setterは素で書く

- [x] 4. **Mapper interface作成**
      `src/main/java/com/example/rental/mapper/CarMapper.java`
      - `List<Car> findAll();` のみ(条件なし全件取得)

- [x] 5. **Mapper XML作成**
      `src/main/resources/mapper/CarMapper.xml`
      - namespace: `com.example.rental.mapper.CarMapper`
      - `findAll` は単純な `SELECT ... FROM car`(`<if>`/`<where>`は使わない)

- [x] 6. **Service作成**
      `src/main/java/com/example/rental/service/CarService.java`
      - `@Service`、コンストラクタインジェクションで`CarMapper`を受け取る
      - `findAll()`が`carMapper.findAll()`をそのまま返す

- [x] 7. **Serviceテスト作成**
      `src/test/java/com/example/rental/service/CarServiceTest.java`
      - Mockitoで`CarMapper`をモック化し、`findAll()`がモックの戻り値をそのまま返すことのみ検証

- [x] 8. **Controller作成**
      `src/main/java/com/example/rental/controller/C1000ListController.java`
      - `@Controller`、`@GetMapping("/c1000list")`
      - `CarService`をコンストラクタインジェクション、結果をModel属性(`carList`)に詰めてビュー名`"c1000/c1000list"`を返す

- [ ] 9. **Controllerテスト作成**
      `src/test/java/com/example/rental/controller/C1000ListControllerTest.java`
      - `@WebMvcTest(C1000ListController.class)` + `@MockitoBean CarService`
      - 検証: ステータス200、ビュー名`c1000/c1000list`、Model属性`carList`の存在

- [x] 10. **messages.properties作成**
      `src/main/resources/messages.properties`
      - `c1000list.title`(車両一覧) / `c1000list.carId` / `c1000list.carName` / `c1000list.numberPlate` / `c1000list.status`
      - 検索・新規登録・編集・削除関連のキーはstep4/5まで作らない

- [x] 11. **テンプレート作成**
      `src/main/resources/templates/c1000/c1000list.html`
      - 最小構成: タイトル + 一覧テーブルのみ
      - `th:each`でModel属性`carList`をループし、`carId`/`carName`/`numberPlate`/`status`を表示。見出しは`th:text="#{c1000list.xxx}"`で参照

- [ ] 12. **手動疎通確認**
      - アプリ起動→ `http://localhost:8080/c1000list` にアクセスし、実DBの`car`テーブルの内容が一覧表示されることを目視確認

## 検証方法

- 単体テスト: `CarServiceTest` / `C1000ListControllerTest` を実行しGreenになることを確認
- 手動確認: アプリ起動後 `/c1000list` にアクセスし、DBの`car`テーブルの内容が一覧表示されることを確認(TODO 12)
