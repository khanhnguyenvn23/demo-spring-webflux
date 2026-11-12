# Demo Spring MVC và Spring WebFlux

Hai ứng dụng Spring Boot độc lập minh họa blocking, non-blocking, streaming và tổng hợp nhiều tác vụ.

## Tổng quan

| Project | Công nghệ | Cổng | Truy cập |
|---|---|---:|---|
| `demospringboot` | Spring MVC, Spring Data JPA | `8081` | `http://localhost:8081` |
| `demowebflux` | Spring WebFlux, Spring Data R2DBC | `8080` | `http://localhost:8080` |

Cả hai dùng H2 trong bộ nhớ và khởi tạo dữ liệu mẫu khi chạy. Yêu cầu Java 17 trở lên. Mỗi project có Maven Wrapper nên không cần cài Maven riêng.

## Khởi chạy

Mở hai terminal tại thư mục repository nếu muốn chạy đồng thời.

**Terminal 1 — Spring MVC**

```powershell
cd .\demospringboot
.\mvnw.cmd spring-boot:run
```

**Terminal 2 — Spring WebFlux**

```powershell
cd .\demowebflux
.\mvnw.cmd spring-boot:run
```

Đợi ứng dụng khởi động xong rồi gửi request đến endpoint tương ứng bên dưới.

## API demo

### Spring MVC — `http://localhost:8081`

- `GET /api/demo/blocking` — Chờ 5 giây rồi đọc sản phẩm; request thread bị chặn.
- `GET /api/demo/aggregate` — Truy vấn số sản phẩm, sản phẩm đắt nhất và giá trung bình theo thứ tự.

### Spring WebFlux — `http://localhost:8080`

- `GET /api/demo/non-blocking` — Đọc sản phẩm bằng R2DBC và chờ bất đồng bộ khoảng 5 giây.
- `GET /api/demo/stream` — Gửi sản phẩm dạng SSE, mỗi sản phẩm cách nhau khoảng 1 giây.
- `GET /api/demo/aggregate` — Chạy ba truy vấn trên bằng `Mono.zip` và trả kết quả gọn.

### Lệnh gọi Spring MVC

```powershell
curl.exe http://localhost:8081/api/demo/blocking
curl.exe http://localhost:8081/api/demo/aggregate
```

### Lệnh gọi Spring WebFlux

```powershell
curl.exe http://localhost:8080/api/demo/non-blocking
curl.exe -N http://localhost:8080/api/demo/stream
curl.exe http://localhost:8080/api/demo/aggregate
```

Tùy chọn `-N` giúp `curl` hiển thị các sự kiện streaming ngay khi nhận được.

## Demo tải đồng thời

Khi cả hai ứng dụng đang chạy, mở terminal tại thư mục repository và chạy:

```powershell
python .\load_test.py --requests 240 --concurrency 240
```

Công cụ gửi cùng số request lần lượt đến MVC `/api/demo/blocking` và WebFlux `/api/demo/non-blocking`, sau đó in số request thành công, tổng thời gian và thời gian phản hồi trung bình/lâu nhất. Cả hai endpoint đều chờ khoảng 5 giây; MVC dùng `Thread.sleep`, còn WebFlux dùng luồng reactive.

Thử tải nhẹ trước bằng `--requests 40 --concurrency 20`. Giá trị tối đa được giới hạn ở 600 request và 600 request đồng thời; script chỉ gọi hai địa chỉ `localhost` đã cấu hình sẵn. Tăng tải từ từ để tránh làm máy quá tải. Đây là demo so sánh trong môi trường local, không phải công cụ benchmark tổng quát. Kết quả phụ thuộc máy và cấu hình; WebFlux vẫn bị giới hạn bởi tài nguyên, event loop và connection pool.

## Ý nghĩa và lưu ý

- **Blocking và non-blocking:** `/blocking` và `/non-blocking` đều có thể mất khoảng 5 giây để trả kết quả. Điểm khác biệt là thao tác chờ trong MVC chặn request thread, còn Reactor không dùng `Thread.sleep` để chờ.
- **Streaming:** `/stream` gửi dữ liệu từng phần để client có thể xử lý trước khi toàn bộ danh sách được phát xong.
- **Tổng hợp tác vụ:** Cả hai endpoint đều truy vấn database để lấy số sản phẩm, sản phẩm có giá cao nhất và giá trung bình. MVC thực hiện lần lượt; WebFlux đăng ký ba truy vấn reactive cùng lúc bằng `Mono.zip`. Thời gian phụ thuộc database và môi trường chạy.

Thời gian chỉ mang tính minh họa và có thể dao động. `Mono.delay` mô phỏng chờ bất đồng bộ; nó không tự biến mọi lời gọi I/O thành reactive hay song song. Trong ứng dụng thực tế, nguồn dữ liệu và client I/O cũng cần hỗ trợ reactive để tận dụng WebFlux. Non-blocking không có nghĩa mỗi tác vụ luôn chạy trên một thread riêng.

## Chạy kiểm thử

Chạy các lệnh sau từ thư mục repository:

```powershell
cd .\demospringboot
.\mvnw.cmd test
cd ..\demowebflux
.\mvnw.cmd test
```
