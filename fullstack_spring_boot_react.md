# 🚀KIẾN TRÚC TỔNG THỂ & LUỒNG CHUYỂN ĐỔI DỮ LIỆU FULLSTACK
> **Kiến trúc:** Database (MySQL) $\rightarrow$ Backend (Spring Boot JPA) $\rightarrow$ REST API (JSON) $\rightarrow$ Frontend (React) $\rightarrow$ UI (HTML/DOM)

---

## 1. Sơ Đồ Liên Kết Tổng Thể (Architectural Flowchart)

```plaintext
┌──────────────────────────────────────────────────────────────────────────────┐
│                            1. DATABASE LAYER                                 │
│                     (MySQL / PostgreSQL / Oracle)                            │
│                     Bảng: products JOIN categories                           │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       │
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                        2. BACKEND LAYER (Spring Boot)                        │
│                                                                              │
│  [ 1. Product.java ]                ──> JPA Entity: Ánh xạ cấu trúc DB       │
│           │                                                                  │
│           ▼                                                                  │
│  [ 2. ProductRepository.java ]      ──> Repository: Thực thi SQL (JOIN/Page) │
│           │                                                                  │
│           ▼                                                                  │
│  [ 3. ProductSummaryResponse.java ] ──> DTO: Thiết kế khuôn mẫu tinh gọn    │
│           │                                                                  │
│           ▼                                                                  │
│  [ 4. ProductServiceImpl.java ]     ──> Service: Biến đổi Entity ➔ DTO      │
│           │                                                                  │
│           ▼                                                                  │
│  [ 5. ProductController.java ]      ──> Controller: Ép DTO ➔ Chuỗi JSON      │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       │
═══════════════════════════════════════╪════════════════════════════════════════ Network (HTTP GET /api/v1/products)
                                       │
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                        3. FRONTEND LAYER (React JS)                          │
│                                                                              │
│  [ 6. productService.js ]           ──> Axios: Gọi API & Parse JSON ➔ Object │
│           │                                                                  │
│           ▼                                                                  │
│  [ 7. ProductList.jsx ]             ──> Component: State ➔ Bơm JSX           │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       │
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                          4. USER INTERFACE LAYER                             │
│                     [ MÀN HÌNH TRÌNH DUYỆT BẰNG DOM HTML ]                   │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Chi Tiết Trực Quan Từng Bước Chuyển Đổi Dữ Liệu

---

### BƯỚC 1: Entity `Product.java` (Ánh Xạ Database)

####  Vai trò
Khai báo cấu trúc dữ liệu bảng `products` trong Database thành một Java Object Model (JPA Entity) trong bộ nhớ RAM.

####  Mã nguồn Code (Java)
```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_product")
    private Long idProduct;

    private String name;
    private String mainImage;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_category")
    private Category category;
}
```

#### Sơ đồ biến đổi dữ liệu
```plaintext
┌──────────────────────────────────────────────────────────────────────────────┐
│ DỮ LIỆU ĐẦU VÀO: Dòng dữ liệu thô trong Database (Bảng products)             │
├────────────┬────────────────────────┬─────────────────────────┬──────────────┤
│ id_product │ name                   │ main_image              │ id_category  │
├────────────┼────────────────────────┼─────────────────────────┼──────────────┤
│ 10         │ Vở kẻ ngang 200 trang  │ /images/vo-ke-ngang.jpg │ 2            │
└────────────┴────────────────────────┴─────────────────────────┴──────────────┘
                                      │
                           [ JPA Mapping Engine ]
                                      ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│ DỮ LIỆU ĐẦU RA: Đối tượng Java trong RAM (Product Entity Object)            │
├──────────────────────────────────────────────────────────────────────────────┤
│ Product Object {                                                             │
│   idProduct = 10L,                                                           │
│   name      = "Vở kẻ ngang 200 trang",                                       │
│   mainImage = "/images/vo-ke-ngang.jpg",                                     │
│   category  = [Proxy Object Category ID 2]                                   │
│ }                                                                            │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

### BƯỚC 2: Repository `ProductRepository.java` (Truy Văn Database)

####  Vai trò
Tạo truy vấn SQL động, tối ưu hóa câu lệnh lấy dữ liệu có phân trang và Fetch Join dữ liệu liên quan để tránh lỗi N+1 SQL performance.

####  Mã nguồn Code (Java)
```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
}
```

####  Sơ đồ biến đổi dữ liệu
```plaintext
INPUT: Tham số truyền vào
  - status = ACTIVE
  - pageable = (page: 0, size: 12)
                          │
                          ▼
             [ SQL Generation Engine ]
                          │
                          ▼
CÂU LỆNH SQL THỰC TẾ CHẠY DƯỚI DATABASE:
  SELECT p.id_product, p.name, p.main_image, c.id_category, c.name AS category_name
  FROM products p
  LEFT JOIN categories c ON p.id_category = c.id_category
  WHERE p.status = 'ACTIVE'
  ORDER BY p.created_at DESC
  LIMIT 12 OFFSET 0;
                          │
                          ▼
OUTPUT: Mảng Java Object
  Page<Product> chứa danh sách Product Entities đã nạp sẵn thông tin Category.
```

---

### BƯỚC 3: DTO `ProductSummaryResponse.java` (Khuôn Mẫu Dữ Liệu Outbound)

####  Vai trò
Định nghĩa một cấu trúc dữ liệu hiển thị (Data Transfer Object) tinh gọn, loại bỏ các thông tin thừa hoặc các liên kết vòng vô tận (`Product` $\rightarrow$ `Category` $\rightarrow$ `List<Product>`).

#### 💻 Mã nguồn Code (Java)
```java
@Data
@Builder
public class ProductSummaryResponse {
    private Long idProduct;
    private String name;
    private String mainImage;
    private String categoryName; // Chỉ giữ lại tên danh mục thay vì cả object Category
}
```

#### 🔄 So sánh cấu trúc
```plaintext
   Cấu trúc Entity ban đầu (Phức tạp & Dễ lặp vô tận):
   Product ────────> Category ────────> List<Product> ────────> Category ...

   Cấu trúc DTO tinh gọn (Tối ưu hóa dung lượng truyền mạng):
   ┌─────────────────────────────────────────┐
   │ ProductSummaryResponse                  │
   ├─────────────────────────────────────────┤
   │ - idProduct    : 10                     │
   │ - name         : "Vở kẻ ngang 200 trang"│
   │ - mainImage    : "/images/vo.jpg"       │
   │ - categoryName : "Dụng cụ học tập"     │
   └─────────────────────────────────────────┘
```

---

### BƯỚC 4: Service `ProductServiceImpl.java` (Ánh Xạ Dữ Liệu Entity $\rightarrow$ DTO)

####  Vai trò
Chứa logic nghiệp vụ thực hiện biến đổi danh sách `Product` Entity từ DB sang danh sách DTO `ProductSummaryResponse`.

####  Mã nguồn Code (Java)
```java
@Override
public Page<ProductSummaryResponse> getActiveProducts(Pageable pageable) {
    Page<Product> products = productRepository.findByStatus(ProductStatus.ACTIVE, pageable);
    return products.map(product -> ProductSummaryResponse.builder()
            .idProduct(product.getIdProduct())
            .name(product.getName())
            .mainImage(product.getMainImage())
            .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
            .build());
}
```

####  Sơ đồ biến đổi dữ liệu (Field-by-Field Mapping)
```plaintext
  Entity Product (Bộ nhớ RAM)                DTO ProductSummaryResponse (Sẵn sàng gửi)
┌─────────────────────────────┐            ┌─────────────────────────────┐
│ idProduct: 10               │ ─────────> │ idProduct: 10               │
│ name: "Vở kẻ ngang..."      │ ─────────> │ name: "Vở kẻ ngang..."      │
│ mainImage: "/vo.jpg"        │ ─────────> │ mainImage: "/vo.jpg"        │
│ category: CategoryObj       │ ──[Lọc]──> │ categoryName: "Dụng cụ"     │
│ description: "Chi tiết..."  │ ──[Bỏ]───> │ (Không chuyển vào DTO)      │
│ variants: List<Variant>     │ ──[Bỏ]───> │ (Không chuyển vào DTO)      │
└─────────────────────────────┘            └─────────────────────────────┘
```

---

### BƯỚC 5: Controller `ProductController.java` (Đóng Gói HTTP Response JSON)

####  Vai trò
Tạo endpoint API tiếp nhận yêu cầu HTTP GET từ client, sử dụng thư viện Jackson để chuyển đổi (Serialize) Java DTO thành chuỗi ký tự JSON chuẩn.

#### 💻 Mã nguồn Code (Java)
```java
@GetMapping
public ResponseEntity<Page<ProductSummaryResponse>> getActiveProducts(Pageable pageable) {
    return ResponseEntity.ok(productService.getActiveProducts(pageable));
}
```

#### 🔄 Sơ đồ chuyển đổi JSON
```plaintext
INPUT: Java Object (Page<ProductSummaryResponse>)
                           │
             [ Jackson JSON Serializer ]
                           ▼
OUTPUT: HTTP Response Body (Gửi qua mạng Internet)
--------------------------------------------------
HTTP/1.1 200 OK
Content-Type: application/json

{
  "content": [
    {
      "idProduct": 10,
      "name": "Vở kẻ ngang 200 trang",
      "mainImage": "/images/vo-ke-ngang.jpg",
      "categoryName": "Dụng cụ học tập"
    }
  ],
  "totalPages": 1,
  "totalElements": 1
}
```

---

### BƯỚC 6: Axios Call `productService.js` (Phía Frontend React)

####  Vai trò
Thực hiện yêu cầu HTTP Network từ Trình duyệt đến Server API, nhận về dữ liệu chuỗi JSON và tự động Parse thành JavaScript Object/Array.

#### 💻 Mã nguồn Code (JavaScript)
```javascript
export const fetchActiveProducts = async (page = 0, size = 12) => {
  const response = await axios.get('/api/v1/products', { params: { page, size } });
  return response.data; 
};
```

#### 🔄 Sơ đồ tiếp nhận dữ liệu
```plaintext
Trình duyệt gửi Request: GET /api/v1/products?page=0&size=12
                           │
             [ Network Request / Proxy ]
                           │
Trình duyệt nhận Raw Text: '{"content":[{"idProduct":10, ...}]}'
                           │
            [ Axios Automatic JSON Parse ]
                           ▼
OUTPUT: JavaScript Object sẵn sàng sử dụng
{
  content: [
    { idProduct: 10, name: "Vở kẻ ngang 200 trang", categoryName: "Dụng cụ học tập", mainImage: "/images/vo-ke-ngang.jpg" }
  ],
  totalPages: 1
}
```

---

### BƯỚC 7: React Component `ProductList.jsx` & Rendering HTML

####  Vai trò
Lưu trữ dữ liệu vào React State (`useState`), dùng cú pháp JSX lặp qua danh sách (`.map()`) để dựng Virtual DOM và yêu cầu Browser Render thành thẻ HTML/CSS thực tế.

####  Mã nguồn Code (JavaScript / JSX)
```javascript
// 1. Cập nhật State
const [products, setProducts] = useState([]);

useEffect(() => {
  fetchActiveProducts(0, 12).then(data => setProducts(data.content));
}, []);

// 2. Duyệt mảng và render JSX
return (
  <div className="product-grid">
    {products.map(product => (
      <div key={product.idProduct} className="card">
        <img src={product.mainImage} alt={product.name} />
        <span className="badge">{product.categoryName}</span>
        <h3>{product.name}</h3>
      </div>
    ))}
  </div>
);
```

####  Sơ đồ rendering ra Màn hình
```plaintext
Biến products trong React State:
[ { idProduct: 10, name: "Vở kẻ ngang 200 trang", categoryName: "Dụng cụ học tập", mainImage: "/images/vo-ke-ngang.jpg" } ]
                                   │
                         [ Vòng lặp .map() JSX ]
                                   ▼
Mã HTML ảo trong bộ nhớ (Virtual DOM):
<div class="card">
  <img src="/images/vo-ke-ngang.jpg" alt="Vở kẻ ngang 200 trang" />
  <span class="badge">Dụng cụ học tập</span>
  <h3>Vở kẻ ngang 200 trang</h3>
</div>
                                   │
                     [ Browser Rendering Engine ]
                                   ▼
MÀN HÌNH HIỂN THỊ THỰC TẾ TRÊN TRÌNH DUYỆT:
┌────────────────────────────────────────────────────────┐
│ ┌────────────────────────────────────────────────────┐ │
│ │                                                    │ │
│ │             [ HÌNH ẢNH SẢN PHẨM ]                  │ │ <── Render từ <img src="..." />
│ │                                                    │ │
│ └────────────────────────────────────────────────────┘ │
│  DỤNG CỤ HỌC TẬP                                      │ <── Render từ <span>...</span>
│  Vở kẻ ngang 200 trang                                │ <── Render từ <h3>...</h3>
└────────────────────────────────────────────────────────┘
```