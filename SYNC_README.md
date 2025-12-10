# Sync System Documentation

## Overview
Hệ thống sync tự động đồng bộ dữ liệu Category và Product với external API endpoints.

## Features
- **Auto Sync**: Background jobs chạy định kỳ để sync data
- **Batch Processing**: Xử lý theo batch để tránh overload
- **Status Tracking**: Theo dõi trạng thái sync của từng record
- **Auto Reset**: Tự động reset sync status khi tạo/cập nhật data

## Sync Status
- `PENDING`: Chưa sync (mặc định khi tạo/cập nhật)
- `SYNCING`: Đang trong quá trình sync
- `SYNCED`: Đã sync thành công
- `FAILED`: Sync thất bại

## API Endpoints

### External Sync Endpoints
- **Category**: `POST https://good-fun.org/category`
- **Product**: `POST https://good-fun.org/product`

### Internal Management Endpoints
- `POST /api/sync/categories` - Manual trigger category sync
- `POST /api/sync/products` - Manual trigger product sync  
- `POST /api/sync/all` - Manual trigger full sync
- `POST /api/sync/products/{id}/generate-string` - Generate product string for specific product

## Configuration
```properties
# Sync URLs
sync.category.url=https://good-fun.org/category
sync.product.url=https://good-fun.org/product

# Batch size (số records xử lý mỗi lần)
sync.batch.size=10

# Enable/disable scheduler
sync.scheduler.enabled=true
```

## Scheduling
- **Category Sync**: Chạy mỗi 5 phút
- **Product Sync**: Chạy mỗi 10 phút

## Data Structure

### Category Sync Payload
```json
{
  "category_id": 15,
  "category_name": "Hoa tươi"
}
```

### Product Sync Payload
```json
{
  "category_id": 15,
  "price": 150000,
  "product_id": 1001,
  "product_name": "Hoa tặng mẹ 21/11",
  "product_string": "Tên sản phẩm: Hoa tặng mẹ 21/11. Mô tả: Hoa màu vàng, tươi mới..."
}
```

## Product String Generation
`product_string` được tự động generate từ:
- Tên sản phẩm
- Mô tả
- Giá
- Số lượng tồn kho
- Loại sản phẩm
- Danh mục
- Thông tin hình ảnh
- Ngày tạo/cập nhật
- Trạng thái

## Database Migration
Chạy script `migration_sync_fields.sql` để thêm các column cần thiết:
```sql
-- Add sync_status to categories
ALTER TABLE categories ADD COLUMN sync_status VARCHAR(20) DEFAULT 'PENDING';

-- Add sync_status and product_string to products
ALTER TABLE products ADD COLUMN sync_status VARCHAR(20) DEFAULT 'PENDING';
ALTER TABLE products ADD COLUMN product_string TEXT;
```

## Monitoring
- Check logs để theo dõi sync process
- Sử dụng database queries để kiểm tra sync status:

```sql
-- Check sync status distribution
SELECT sync_status, COUNT(*) FROM categories GROUP BY sync_status;
SELECT sync_status, COUNT(*) FROM products GROUP BY sync_status;

-- Find failed syncs
SELECT * FROM categories WHERE sync_status = 'FAILED';
SELECT * FROM products WHERE sync_status = 'FAILED';
```

## Troubleshooting
1. **Sync fails**: Check external API availability
2. **Performance issues**: Adjust `sync.batch.size`
3. **Disable sync**: Set `sync.scheduler.enabled=false`
4. **Manual retry**: Use `/api/sync/*` endpoints