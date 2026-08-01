Code Quality & Technical Debt Audit Report
Dưới đây là báo cáo đánh giá chi tiết codebase của dự án University ERP bao gồm cả frontend và backend, dựa trên các yêu cầu phân tích của bạn.

1. Tuân thủ SOLID, DRY, KISS trong Services và Controllers
Nhìn chung, dự án có phân tách được các Controller và Service. Tuy nhiên, vẫn còn nhiều vi phạm nghiêm trọng về các nguyên tắc SOLID, DRY (Don't Repeat Yourself) và KISS (Keep It Simple, Stupid).

WARNING

Top 3 class vi phạm nặng nhất và cách khắc phục:

LuongService.java

Vi phạm: Vi phạm SRP (Single Responsibility Principle) và DRY nặng nề. Class này đang "ôm đồm" quá nhiều việc: check authorization thủ công, query database, serialize JSON thủ công bằng ObjectMapper, publish message qua WebSockets.
Cách sửa:
Chuyển logic phân quyền (isAdminOrGiaoVu) lên annotation @PreAuthorize ở Controller hoặc tách ra thành một SecurityService.
Tách logic xử lý JSON sang một Utility/Converter.
Tách phần gửi thông báo WebSocket sang một service/event listener riêng (Dùng Spring ApplicationEventPublisher).
AuthServiceImpl.java

Vi phạm: DRY bị phá vỡ. Đoạn code tạo RefreshToken, tính toán thời gian hết hạn (new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000)), và parse Roles từ UserDetails bị lặp lại y hệt trong 2 hàm login() và refreshAccessToken().
Cách sửa: Gộp logic tạo cấu trúc Token thành một hàm private dùng chung.
ScheduleJobConsumer.java

Vi phạm: SRP. Consumer đang thực thi JMS logic, tự lưu cache vào Redis, và tự publish WebSockets.
Cách sửa: Delegate việc lưu Redis và tính toán vào tầng Service (VD: ScheduleOptimizationService), Consumer chỉ nên nhận message và điều phối.
2. Kiểm tra việc xử lý ngoại lệ (Error Handling)
GlobalExceptionHandler đã được setup khá tốt khi bao phủ được nhiều loại Exception như AccessDeniedException, MethodArgumentNotValidException, ConflictException, v.v. và trả về format ErrorResponseDTO thống nhất.

CAUTION

Vấn đề nghiêm trọng (Swallowed Exceptions): Có một số nơi trong codebase đang lạm dụng try-catch và "nuốt" lỗi (không re-throw hoặc xử lý triệt để), điều này sẽ gây khó khăn lớn khi debug trên Production:

AuthServiceImpl.java:92
: Hàm logout đang catch Exception và bỏ qua hoàn toàn (// Không ném exception — logout vẫn thành công về mặt client). Nếu database hoặc Redis có vấn đề lúc blacklist token, hệ thống sẽ không có báo cáo lỗi.
ScheduleJobConsumer.java:59
: Catch JsonProcessingException khi gửi lỗi fallback, chỉ log ra chứ không xử lý tiếp, có thể khiến message bị treo.
Nhiều hàm khác trong dự án xử lý logic Database chỉ in ra log thay vì ném ra custom exception để controller/middleware xử lý chung.
3. Đánh giá Test Coverage và Chiến lược Test
Hiện trạng:

Backend: Thư mục src/test/java/com/wiz/universityerpapi/service hiện có 4 file test (LuongServiceTest.java, VNPayServiceTest.java, ScheduleOptimizationServiceTest.java, AuthorizationTest.java) nhưng nhìn chung số lượng còn rất hạn chế.
Frontend: Hoàn toàn thiếu vắng các test file (không có các file .test.tsx hay .spec.ts cho các thành phần quan trọng).
TIP

Đề xuất chiến lược cho các module quan trọng:

Module Tính Lương (LuongService):

Unit Test: Phải có test độc lập (mock database) để kiểm tra các công thức toán học (Tổng Lương = Lương Cơ Bản + (Số tiết * Đơn giá * Hệ số)). Test cho các Edge Cases: Không có tiết dạy, Hệ số null, v.v.
Integration Test: Test từ đầu tới cuối việc lưu chuỗi JSONB lịch sử bảng lương xuống Postgres xem có chính xác không (dùng Testcontainers).
Module Thanh Toán (VNPayService / PaymentController):

Unit Test: Tạo test cover logic mã hóa HMAC (checksum) xem có khớp định dạng mà VNPay quy định hay không, test luồng parse IPN (Webhook) callback.
Integration Test: Giả lập callback request từ hệ thống VNPay bằng MockMvc để test update lại trạng thái đóng học phí mà không cần Sandbox thực.
4. Phân tích Cấu trúc Thư mục (Scalability)
Hiện trạng: Cả Frontend và Backend đều đang áp dụng mô hình tổ chức thư mục kiểu Package-by-Layer (Chia theo lớp).

Backend: controller/, service/, entity/, repository/.
Frontend: components/, pages/, api/, store/.
IMPORTANT

Đánh giá mở rộng (5 lên 20 người): Mô hình hiện tại không phù hợp để mở rộng team lên con số 20. Khi team phân tách làm nhiều squad/squad nhỏ (VD: Team Học phí, Team Lương, Team Đào tạo), việc tất cả mọi người cùng sửa và thêm file trong thư mục controllers/ hay pages/ sẽ sinh ra Merge Conflict liên tục và rất khó kiểm soát code domain của nhau.

Giải pháp: Chuyển sang mô hình Package-by-Feature (Module hóa / Feature-Sliced Design).

Backend: Nhóm code theo nghiệp vụ/domain. Ví dụ: payroll/ (chứa LuongController, LuongService, CauHinhLuong, BangLuongRepository), schedule/.
Frontend: Nhóm theo feature. Ví dụ: features/salary/ (chứa component, hook, api riêng của lương), features/schedule/. Thư mục components gốc chỉ nên chứa System UI kit dùng chung (như Button, Table).
5. Top 3 Khoản "Nợ Kỹ Thuật" Cần Refactor Ngay
WARNING

Đây là các khoản nợ kỹ thuật (Technical Debt) ảnh hưởng lớn tới sự ổn định và có nguy cơ phình to, cần đưa vào Sprint refactor ngay trong tháng này:

Giải phóng "Fat Service" ở 
LuongService
: Service này quá cồng kềnh. Cần tách logic Async WebSockets và Authorization ra khỏi logic tính lương cốt lõi. Chuyển logic giao tiếp WebSockets sang sử dụng Spring ApplicationEventPublisher.
Loại bỏ tình trạng "Swallowed Exceptions": Kiểm tra lại toàn bộ các block catch (Exception e). Ngoại lệ không thể bị "nuốt" âm thầm bằng comment và log.warn(). Hãy bọc chúng vào trong CustomException rồi ném ra lại (throw new ...) để GlobalExceptionHandler thống nhất xử lý kết quả trả về cho client.
Tái cấu trúc thư mục (Migrate to Feature-based): Trước khi dự án và nhân sự phình to hơn, phải chuyển đổi thư mục Backend và Frontend sang dạng Bounded Contexts / Features để đảm bảo ranh giới mã nguồn rõ ràng, giúp chia task cho các team song song một cách dễ dàng và tránh bị dính chặt (tight-coupled).

Báo Cáo Đánh Giá Năng Lực Mở Rộng (Scalability) Hệ Thống ERP
Chào bạn, với tư cách là một Cloud Solutions Architect, tôi đã tiến hành audit codebase của dự án university_erp. Dưới đây là báo cáo đánh giá chi tiết về kiến trúc hiện tại và lộ trình tối ưu hóa.

1. Phân Tích Mô Hình Kiến Trúc Hiện Tại
Dựa vào cấu trúc mã nguồn (các package như controller, service, repository, entity...) và docker-compose.yml, hệ thống hiện tại đang được xây dựng theo mô hình Monolith (Kiến trúc nguyên khối) với cấu trúc Package-by-Layer kết hợp với một Sidecar Service nhỏ (AI Forecaster bằng Python).

NOTE

Mô hình Monolith rất phù hợp ở giai đoạn đầu của dự án vì dễ phát triển, dễ debug và triển khai (chỉ cần 1 container erp-backend).

Tuy nhiên, đối với một hệ thống ERP Trường Đại Học, mô hình này bộc lộ những hạn chế lớn khi mở rộng:

Nghiệp vụ không đồng đều về tải: Module "Đăng ký tín chỉ" có tải cực kỳ cao nhưng chỉ diễn ra trong vài ngày, trong khi module "Chốt lương" tiêu tốn nhiều CPU/RAM nhưng tần suất thấp (1 lần/tháng). Với Monolith, bạn buộc phải scale toàn bộ ứng dụng, gây lãng phí tài nguyên khổng lồ.
Tính đóng gói (Coupling): Việc dùng Package-by-Layer (gom tất cả controller vào một chỗ, service vào một chỗ) làm giảm tính độc lập của các nghiệp vụ, dẫn đến khó tách microservice sau này.
2. Đánh Giá Cơ Chế Multi-Tenancy và Phân Tách Dữ Liệu
Hệ thống đã triển khai cơ chế Multi-Tenancy rất tốt thông qua Hibernate, cụ thể ở cấu hình:

yaml

multiTenancy: SCHEMA
tenant_identifier_resolver: com.wiz.universityerpapi.tenant.CurrentTenantIdentifierResolverImpl
TIP

Schema-based Multi-Tenancy là một lựa chọn xuất sắc (Sweet spot). Nó đảm bảo an toàn dữ liệu cao hơn Column-based (không lo rò rỉ chéo do quên điều kiện WHERE tenant_id = ?) và tiết kiệm chi phí hơn Database-per-tenant.

Rủi ro tiềm ẩn (Noisy Neighbor): Vì các Schema vẫn nằm chung trên một Instance PostgreSQL (erp-postgres), nếu một trường đại học (Tenant A) chạy tiến trình chốt lương nặng hoặc truy xuất báo cáo lớn, nó sẽ tiêu thụ toàn bộ CPU/IOPS của DB. Điều này sẽ làm Tenant B bị chậm (hiệu ứng Noisy Neighbor).

3. Xác Định Bottleneck Khi Chịu Tải 10,000 CCU
Khi hệ thống đối mặt với 10,000 CCU (ví dụ: sinh viên ồ ạt đăng ký tín chỉ), hệ thống sẽ gãy (crash) ngay lập tức tại các điểm sau:

CAUTION

Điểm nghẽn chí tử: Connection Pool (HikariCP) Trong application.yaml, connection pool đang cấu hình:

write.maximum-pool-size: 10
read.maximum-pool-size: 20 10,000 requests đồng thời đổ vào sẽ làm cạn kiệt 10 connection này ngay trong mili-giây đầu tiên, dẫn đến lỗi ConnectionTimeoutException hàng loạt và Thread Starvation trên Tomcat.
Các điểm nghẽn khác:

Database Locks: Các giao dịch đăng ký tín chỉ yêu cầu tính toàn vẹn (tránh đăng ký quá sĩ số). Việc lock row trên PostgreSQL dưới tải cao sẽ gây deadlocks hoặc timeout.
Đồng bộ hóa giao dịch (Synchronous DB Write): Các hàm xử lý nặng (như chotLuongThang trong LuongService.java) sử dụng @Transactional thực hiện tính toán và ghi Bulk Update trực tiếp, giam giữ DB Connection trong thời gian dài.
4. Đánh Giá Việc Sử Dụng Message Queue, Async Task và WebSocket
Hệ thống đã áp dụng các pattern bất đồng bộ, cụ thể:

ActiveMQ & @JmsListener (ScheduleJobConsumer.java): Đã áp dụng chuẩn xác cho bài toán Xếp lịch tự động (Schedule Optimization). Đẩy task nặng vào Queue và cho worker xử lý dần là best practice.
@Async (LuongService.java): Áp dụng để tính lương bất đồng bộ (chotLuongThangAsync).
WebSocket: Sử dụng kết hợp với ActiveMQ để đẩy (push) kết quả về cho client thay vì bắt client phải Long-Polling. (e.g., /topic/schedule/status/ và /queue/notifications).
WARNING

Mặc dù @Async giúp giải phóng luồng HTTP (Tomcat thread), nhưng bản chất bên trong hàm tính lương vẫn gọi tới DB trực tiếp. Do đó, DB Connection vẫn bị chiếm dụng. Nếu nhiều người cùng nhấn "Chốt lương", Database vẫn quá tải dù Backend không bị treo HTTP.

5. Lộ Trình (Roadmap) Chuyển Đổi Lên Kubernetes (K8s) & Auto-Scaling
Để đưa hệ thống này chịu tải được 10,000 CCU và tận dụng được tính năng Auto-Scaling của K8s, tôi đề xuất Roadmap 3 giai đoạn như sau:

Phase 1: Tối Ưu Hóa & Tách Rời (Decoupling) trên kiến trúc hiện tại
Chuyển sang Modular Monolith: Đổi cấu trúc thư mục từ Layered sang Package-by-Feature (e.g., modules/payroll, modules/registration). Định nghĩa rõ interface giao tiếp giữa các module (không gọi trực tiếp Repository của module khác).
PgBouncer & Connection Tuning: Đưa PgBouncer vào trước PostgreSQL để quản lý Connection Pooling cấp độ Database, thay vì phụ thuộc hoàn toàn vào HikariCP của Spring. Tăng pool size một cách có tính toán.
Asynchronous Registration: Không ghi thẳng dữ liệu Đăng ký tín chỉ vào DB. Đưa request đăng ký vào ActiveMQ/Kafka, trả về HTTP 202 Accepted. Consumer sẽ tuần tự xử lý hàng đợi và báo kết quả qua WebSocket.
Phase 2: Phân Tách Microservices & Containerization
Bóc tách các module có "Traffic Profile" khác biệt thành các service độc lập:
registration-service: Scale mạnh lúc đăng ký tín chỉ (Yêu cầu CPU cao, short-lived).
payroll-service: Scale mạnh cuối tháng.
core-service: Quản lý sinh viên, danh mục (Traffic ổn định).
Stateless Applications: Đảm bảo mọi Microservices đều Stateless (không lưu session nội bộ, dùng Redis cho Session/Cache như cấu hình hiện tại đang làm khá tốt).
Viết Helm Charts định nghĩa Deployment, Service, ConfigMap và Secrets cho từng service.
Phase 3: Triển Khai Lên K8s & Kích Hoạt HPA/KEDA
Horizontal Pod Autoscaler (HPA): Cấu hình tự động scale số lượng Pods của registration-service dựa trên CPU (>70%) và Memory.
KEDA (Kubernetes Event-driven Autoscaling): Tích hợp KEDA với ActiveMQ. Khi số lượng message Đăng ký tín chỉ hoặc Xếp lịch trong Queue tăng vọt, KEDA sẽ tự động scale worker pods (thậm chí scale từ 0 lên N) để xử lý dữ liệu nhanh chóng.
Managed Database: Chuyển từ DB chạy trong container (erp-postgres) sang các dịch vụ Managed Database (như AWS RDS, Google Cloud SQL) để đảm bảo High Availability và Auto-scaling cho Storage/IOPS. Đưa Read-Replica vào hoạt động thực tế.

Báo Cáo Phân Tích Database, Entity & Hiệu Năng Hệ Thống (DBA & Performance Engineering)
Dựa trên quá trình rà soát toàn diện mã nguồn backend (đặc biệt là các module quản lý Lịch Học và Tính Lương), dưới đây là báo cáo phân tích và các đề xuất tối ưu hóa hiệu năng hệ thống.

1. Phân Tích Lỗi N+1 Queries trong Hibernate/JPA
TIP

Điểm cộng: Các Entity đa số đã được cấu hình @ManyToOne(fetch = FetchType.LAZY) (ví dụ trong GiangVien, SinhVien, LopHocPhan), đây là practice rất tốt để tránh N+1 mặc định của JPA. Đồng thời UserRepository đã sử dụng đúng JOIN FETCH u.roles.

Tuy nhiên, hệ thống vẫn tồn tại lỗ hổng N+1 Queries nghiêm trọng trong module Xếp lịch học:

Đích danh hàm gây lỗi: Trong 
LichHocChiTietRepository.java
, phương thức findConflictingRoomSchedule và findConflictingLecturerSchedule sử dụng JOIN nhưng không sử dụng JOIN FETCH:

java

@Query("SELECT th FROM TuanHocChiTiet th " +
       "JOIN th.lichHocChiTiet lh " + // Vấn đề ở đây: JOIN thay vì JOIN FETCH
       "WHERE lh.phongHoc = :phongHoc ...")
List<TuanHocChiTiet> findConflictingRoomSchedule(...)
Hậu quả: Khi 
LichHocService.java (Dòng 57)
 gọi:

java

conflict.getLichHocChiTiet().getPhongHoc()
Hibernate sẽ lập tức bắn thêm 1 query phụ để lấy thông tin LichHocChiTiet từ Database, gây ra N+1 Queries nếu lặp qua danh sách conflicts.

Giải pháp đề xuất: Thay thế JOIN bằng JOIN FETCH trong các Repository:

diff

- JOIN th.lichHocChiTiet lh
+ JOIN FETCH th.lichHocChiTiet lh
2. Đánh Giá Thiết Kế Schema (Normalization vs Denormalization)
IMPORTANT

Thiết kế cực kỳ xuất sắc ở module Tính Lương (BangLuongThang).

Phi chuẩn hóa (Denormalization) hợp lý: Bảng BangLuongThang đã sử dụng kỹ thuật Snapshot (heSoCdSnapshot, donGiaTietSnapshot) và lưu vết toàn bộ công thức/nhật ký dạy vào chuỗi JSON (chiTietTinhLuongJson). Điều này tuân thủ chuẩn kế toán: dữ liệu tài chính trong quá khứ không được bị thay đổi nếu hệ số hoặc đơn giá thay đổi ở hiện tại.
Chuẩn hóa (Normalization) tốt ở Lịch Học: Việc tách LichHocChiTiet và TuanHocChiTiet là cách tiếp cận chuẩn xác để giải quyết bài toán lớp học diễn ra vào các tuần không liên tiếp.
Đề xuất cải thiện: Trường chiTietTinhLuongJson nên được chuyển kiểu dữ liệu DB thành JSONB (nếu dùng PostgreSQL) hoặc JSON (MySQL) để có thể query trực tiếp các metadata bên trong log khi cần audit.

3. Chiến Lược Đánh Index (Chỉ Mục)
Kiểm tra các Entities thông qua @Table cho thấy hệ thống hiện không định nghĩa các Index ở tầng cấu hình JPA. Nếu DB không được đánh Index thủ công qua Script SQL, hệ thống sẽ sập khi đạt ngưỡng 10,000 CCU do Full Table Scan.

Các Index cấp bách cần bổ sung:

Bảng	Cột Cần Index	Lý do (Query thực tế)
bang_luong_thang	(ma_gv, thang, nam)	Phục vụ existsByMaGvAndThangAndNam và findByMaGvAndThangAndNam chạy rất thường xuyên trong LuongService.
lich_hoc_chi_tiet	(phong_hoc, thu_trong_tuan)	Tối ưu truy vấn kiểm tra trùng lịch phòng học (findConflictingRoomSchedule).
phan_cong_day	(ma_lop_hp, ma_gv)	PhanCongDayRepository.findMaGvByMaLopHp đang query liên tục qua cột này.
nhat_ky_giang_day	(ma_gv, trang_thai_thanh_toan)	Hàm findUnpaidLogsByGvAndDateRange dùng để chốt lương. Cần Partial Index cho trường hợp trang_thai_thanh_toan = false.
4. Phân Tích Chiến Lược Caching (Redis)
Việc sử dụng Redis (RedisConfig.java) với serialization JSON là rất tốt. Các phân vùng TTL được thiết lập hợp lý.

WARNING

Cảnh báo Cache Stale Data (Dữ liệu cũ): Cache cau_hinh_luong đang có TTL là 24 giờ. Nếu admin phòng nhân sự cập nhật đơn giá lương, hệ thống sẽ mất tối đa 24 giờ để làm mới. Lẽ ra cấu hình thay đổi thì áp dụng ngay. Cách sửa: Thêm @CacheEvict(value = "cau_hinh_luong", allEntries = true) vào hàm Update/Create của CauHinhLuongService.

CAUTION

Rủi ro bùng nổ RAM (Cache Eviction Risk): Trong DashboardService, Key cache là thang-nam. Tuy nhiên TTL chỉ là 5 phút. Nếu người dùng chuyển qua lại liên tục giữa hàng chục tháng, cache keys sẽ sinh ra rác liên tục. TTL 5 phút là khá ngắn, nếu Dashboard truy xuất quá nặng, DB vẫn sẽ gánh tải.

5. Phương Án Tối Ưu Hóa Hiệu Năng (Latency)
A. API Xếp Lịch Học (ScheduleOptimizationService)
Tình trạng: OR-Tools (CP-SAT) đang chạy tuần tự từng Batch 50 lớp. Thuật toán này rất ngốn CPU và Time. 1000 lớp sẽ chạy thành 20 vòng lặp tuần tự.
Tối ưu: Khai thác Multi-Threading. Sử dụng CompletableFuture kết hợp với custom ThreadPoolExecutor để chạy song song các Batch không liên quan với nhau về mặt giảng viên hoặc phòng. Cấu hình cấp số lượng NumSearchWorkers cho OR-Tools Solver tương ứng với số nhân CPU.
B. API Tính Lương (LuongService.chotLuongThangAsync)
Tình trạng: Mặc dù đã dùng @Async trả kết quả nhanh cho Frontend (thông báo qua WebSocket), ở bên dưới DB vẫn là vòng lặp lấy từng NhatKyGiangDay, tổng hợp và thực hiện Bulk Update.
Tối ưu:
Tránh ném toàn bộ Object Entities vào Memory khi duyệt NhatKyGiangDay. Sử dụng JPQL Projection (chỉ lấy mã nhật ký và số tiết).
Chuyển đổi kiến trúc sang Spring Batch nếu quy mô giảng viên vượt mức 5,000 người. Phân chia Job thành các Step: Reader (Đọc logs chưa thanh toán theo chunk) -> Processor (Tính toán JSON) -> Writer (Insert BangLuong + Update trạng thái).

Báo Cáo Đánh Giá Kiến Trúc AI & Định Hướng Công Nghệ - University ERP
Người thực hiện: AI/Data Architect & Tech Lead Dự án: University ERP (Salary Forecaster & AI Microservices) Ngày thực hiện: 01/08/2026

1. Đánh Giá Chất Lượng Code Tích Hợp AI (Prophet & Fallback)
Dựa trên việc kiểm tra mã nguồn ai-services/forecaster/app.py, dưới đây là những đánh giá về chất lượng triển khai mô hình Prophet và cơ chế dự phòng:

Ưu Điểm (Tư Duy Kiến Trúc Tốt)
TIP

Cơ chế Fallback (High Availability) Việc triển khai thuật toán "Harmonic Fourier Trend Regression" làm dự phòng khi Prophet bị lỗi (hoặc khi không đủ số tháng lịch sử) là một tư duy rất tuyệt vời. Nó đảm bảo API luôn trả về kết quả cho Frontend/Backend mà không bị gián đoạn.

NOTE

Domain Knowledge (Kiến Thức Nghiệp Vụ) Cấu hình SEASONAL_CONFIG (phân biệt học kỳ 1, học kỳ 2, nghỉ hè) và áp dụng seasonality_mode='multiplicative' trong Prophet cho thấy sự hiểu biết sâu sắc về đặc thù chu kỳ tài chính của môi trường Đại học.

Điểm Cần Cải Thiện & Rủi Ro Nghiêm Trọng
CAUTION

Training On-the-fly (Anti-pattern) Hàm m.fit(df) (huấn luyện mô hình) đang được gọi ngay bên trong endpoint @app.post("/forecast"). Prophet training tốn tài nguyên CPU và thời gian. Khi lượng dữ liệu lớn dần hoặc có nhiều request cùng lúc, API sẽ bị nghẽn (timeout) và hệ thống có nguy cơ treo.

Giải pháp: Cần tách biệt rõ Pipeline Training (chạy batch định kỳ để train và lưu model ra file) và Pipeline Inference (FastAPI lúc này chỉ load model lên và gọi m.predict()).

WARNING

Thiếu bóng dáng LangChain & Logic Import Mặc dù LangChain được đề cập trong mục tiêu dự án, nhưng hiện tại trong codebase forecaster chưa có dấu vết của các tác vụ NLP/LLM. Ngoài ra, việc import thư viện Prophet bên trong khối try...except lúc runtime là cách làm "chữa cháy". Ở môi trường Production, requirements (Docker) phải đảm bảo thư viện luôn tồn tại và nên import ở đầu file.

2. Đề Xuất Tối Ưu Giao Tiếp Backend (Spring Boot) & AI (Python)
Việc sử dụng REST HTTP (FastAPI) hiện tại dễ triển khai nhưng độ trễ cao do chi phí parse JSON. Tùy thuộc vào Use-case, hệ thống nên chuyển dịch sang các mô hình kiến trúc sau:

Phương án 1: gRPC (Tác Vụ Real-time / Đồng Bộ)
Áp dụng khi: Người dùng (Ví dụ: Trưởng phòng Tài chính) bấm nút "Xem dự báo" trên UI và đứng đợi kết quả.
Ưu điểm: gRPC sử dụng Protobuf truyền tải dữ liệu nhị phân cực nhanh, serialize/deserialize nhanh gấp 5-10 lần JSON. Đảm bảo hợp đồng dữ liệu chặt chẽ (type-safe) giữa Java và Python.
Phương án 2: Message Broker với Kafka / RabbitMQ (Tác Vụ Batch / Async)
Áp dụng khi: Cần chạy mô hình AI tốn nhiều thời gian, hoặc tính toán dự báo cho hàng ngàn nhân sự/dữ liệu lớn cùng lúc.
Ưu điểm: Spring Boot chỉ cần phát sự kiện SalaryForecastRequested vào Kafka rồi trả về cho user "Hệ thống đang tính toán". Python service đóng vai trò Consumer, lấy task về chạy ngầm, sau đó bắn kết quả SalaryForecastCompleted vào Kafka để Spring Boot lưu DB và thông báo (Websocket) cho user. Tránh việc HTTP request bị timeout.
3. Gợi Ý 3 Tính Năng AI Đột Phá Cho University ERP
Dữ liệu ERP (nhân sự, điểm số, học phí, lịch học) là nền tảng vững chắc để triển khai các tính năng AI nâng cao:

AI RAG Assistant (Trợ Lý Nghiệp Vụ Toàn Diện)

Công nghệ: LangChain + Vector Database (Milvus/pgvector) + LLM.
Ứng dụng: Cho phép hỏi đáp ngôn ngữ tự nhiên trên các dữ liệu phi cấu trúc. VD: “Quy chế học vụ năm nay có gì đổi mới về điểm rèn luyện?”, “Tuần sau tôi có lịch gác thi ở cơ sở nào?”. Giúp số hóa hoàn toàn nghiệp vụ hỗ trợ sinh viên.
Student Churn / Dropout Prediction (Dự Báo Sinh Viên Bỏ Học)

Công nghệ: Machine Learning truyền thống (XGBoost, Random Forest).
Ứng dụng: Phân tích lịch sử điểm danh, điểm số, thanh toán học phí để cảnh báo sớm xác suất sinh viên sắp bỏ học hoặc nợ môn nghiêm trọng. Hệ thống tự động báo cho Cố vấn học tập để có biện pháp can thiệp.
Tối Ưu Hóa Lịch Giảng Dạy & Xếp Phòng Thi

Công nghệ: Operations Research, Reinforcement Learning hoặc Genetic Algorithms.
Ứng dụng: Tự động xếp hàng ngàn lớp học, phòng thi, giáo viên gác thi sao cho không trùng giờ, giảm thời gian di chuyển giữa các cơ sở, và tối ưu tỷ lệ lấp đầy phòng học.
4. Lộ Trình Nâng Cấp Tech Stack (1-2 Năm Tới)
Để đảm bảo hệ thống không bị "legacy", lộ trình nâng cấp kiến trúc nên bao gồm:

Tầng Data & Machine Learning:
Chuyển từ Pandas sang Polars để tăng tốc độ xử lý Data Prep lên từ 10x-50x.
Tích hợp MLflow để quản lý phiên bản các mô hình AI (Prophet, XGBoost).
Tầng Backend (Java Spring Boot):
Nâng cấp lên Spring Boot 3.x + Java 21, áp dụng Virtual Threads (Project Loom) để tăng khả năng chịu tải I/O non-blocking.
Tầng AI Serving (Python):
Chuyển đổi sang Ray Serve hoặc Triton Inference Server khi bắt đầu chạy các mô hình LLM hoặc Deep Learning nặng thay vì chỉ dùng FastAPI.
Tầng Infrastructure & Database:
Tích hợp extension pgvector cho PostgreSQL để phục vụ tính năng RAG.
Triển khai Kubernetes (K8s) với KEDA (Kubernetes Event-driven Autoscaling) để tự động scale số lượng container Python AI dựa trên số lượng message trong Kafka vào kỳ cao điểm.
