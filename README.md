# Oracle JDBC API (Java + Spring Boot)

## 手動構成手順

1. Oracle公式サイトから `ojdbc11.jar` をダウンロード
2. プロジェクトの `lib/` フォルダに配置
3. `pom.xml` に system scope で依存関係を追加済み

## Dockerでのビルドと実行

```bash
docker build -t oracle-jdbc-api .
docker run -p 8080:8080 \
  -e ORACLE_JDBC_URL=jdbc:oracle:thin:@//your-host:1521/your-service \
  -e ORACLE_JDBC_USER=your_user \
  -e ORACLE_JDBC_PASSWORD=your_password \
  oracle-jdbc-api
