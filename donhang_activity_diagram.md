# Activity Diagram - Quy Trình Đơn Hàng

## Mermaid Activity Diagram

```mermaid
flowchart TD
    Start((Start)) --> CreateOrder[Tạo đơn hàng]
    CreateOrder --> CheckStock[Kiểm tra tồn kho]
    
    CheckStock -->|Hết hàng| NotifyOutOfStock[Thông báo hết hàng]
    NotifyOutOfStock --> End((End))
    
    CheckStock -->|Đủ hàng| SetStatus1[Trạng thái: Mới tạo<br/>TT_MOI_TAO = 1]
    SetStatus1 --> NotifyPartner[Gửi thông báo cho đối tác]
    
    NotifyPartner --> PartnerAction{Đối tác xử lý}
    
    PartnerAction -->|Từ chối| SetStatus8[Trạng thái: Đối tác từ chối<br/>TT_DOI_TAC_TU_CHOI = 8]
    SetStatus8 --> RestoreStock[Cộng lại tồn kho]
    RestoreStock --> NotifyReject[Thông báo nhân viên]
    NotifyReject --> End
    
    PartnerAction -->|Chấp nhận| SetStatus2[Trạng thái: Chờ đối tác xác nhận<br/>TT_CHO_DOI_TAC_XAC_NHAN = 2]
    SetStatus2 --> SetDeliveryDate[Đặt ngày giao dự kiến]
    SetDeliveryDate --> SetStatus3[Trạng thái: Đã xác nhận<br/>TT_DA_XAC_NHAN = 3]
    
    SetStatus3 --> SetStatus4[Trạng thái: Đang xử lý<br/>TT_DANG_XU_LY = 4]
    SetStatus4 --> Processing{Xử lý đơn hàng}
    
    Processing -->|Gặp sự cố| SetStatus11[Trạng thái: Gặp sự cố<br/>TT_GAP_SU_CO = 11]
    SetStatus11 --> ReportIssue[Báo cáo sự cố]
    ReportIssue --> ResolveIssue{Giải quyết sự cố}
    
    ResolveIssue -->|Hủy đơn| SetStatus7[Trạng thái: Đã hủy<br/>TT_DA_HUY = 7]
    SetStatus7 --> RestoreStock2[Cộng lại tồn kho]
    RestoreStock2 --> End
    
    ResolveIssue -->|Tiếp tục| SetStatus4
    
    Processing -->|Hoàn thành xử lý| SetStatus9[Trạng thái: Đã giao<br/>TT_DA_GIAO = 9]
    SetStatus9 --> SetStatus5[Trạng thái: Chờ thanh toán<br/>TT_CHO_THANH_TOAN = 5]
    
    SetStatus5 --> Payment{Thanh toán}
    
    Payment -->|Chưa thanh toán| WaitPayment[Chờ thanh toán]
    WaitPayment --> Payment
    
    Payment -->|Đã thanh toán| SetStatus10[Trạng thái: Đã thanh toán<br/>TT_DA_THANH_TOAN = 10]
    SetStatus10 --> SetStatus6[Trạng thái: Hoàn thành<br/>TT_HOAN_THANH = 6]
    
    SetStatus6 --> CreateContract[Tạo hợp đồng]
    CreateContract --> CreateInvoice[Tạo hóa đơn]
    CreateInvoice --> CreateDebt[Tạo công nợ]
    CreateDebt --> End
    
    %% Swimlanes
    subgraph Nhân_Viên
        CreateOrder
        CheckStock
        NotifyOutOfStock
        SetStatus1
        NotifyPartner
        NotifyReject
        RestoreStock2
        ReportIssue
        CreateContract
        CreateInvoice
        CreateDebt
    end
    
    subgraph Đối_Tác
        PartnerAction
        SetDeliveryDate
    end
    
    subgraph Hệ_Thống
        SetStatus2
        SetStatus3
        SetStatus4
        SetStatus8
        SetStatus9
        SetStatus5
        SetStatus10
        SetStatus6
        SetStatus11
        SetStatus7
        RestoreStock
        Processing
        Payment
        WaitPayment
        ResolveIssue
    end
    
    %% Styling
    classDef startend fill:#e1f5e1,stroke:#4caf50,stroke-width:2px
    classDef process fill:#e3f2fd,stroke:#2196f3,stroke-width:2px
    classDef decision fill:#fff3e0,stroke:#ff9800,stroke-width:2px
    classDef error fill:#ffebee,stroke:#f44336,stroke-width:2px
    classDef success fill:#f1f8e9,stroke:#8bc34a,stroke-width:2px
    
    class Start,End startend
    class CreateOrder,CheckStock,SetStatus1,SetStatus2,SetStatus3,SetStatus4,SetStatus5,SetStatus6,SetStatus9,SetStatus10,SetDeliveryDate,CreateContract,CreateInvoice,CreateDebt process
    class PartnerAction,Processing,Payment,ResolveIssue decision
    class SetStatus7,SetStatus8,SetStatus11,NotifyOutOfStock error
    class SetStatus6 success
```

## PlantUML Activity Diagram

```plantuml
@startuml ActivityDiagram_DonHang

skinparam activity {
  BackgroundColor #e3f2fd
  BorderColor #2196f3
}

skinparam swimlane {
  BorderColor #2196f3
}

|Nhân Viên|
start
:Tạo đơn hàng;
:Kiểm tra tồn kho;

if (Hết hàng?) then (yes)
  :Thông báo hết hàng;
  stop
else (no)
  :Trạng thái: Mới tạo (TT_MOI_TAO = 1);
  :Gửi thông báo cho đối tác;
endif

|Đối Tác|
if (Đối tác xử lý?) then (Từ chối)
  |Hệ Thống|
  :Trạng thái: Đối tác từ chối (TT_DOI_TAC_TU_CHOI = 8);
  :Cộng lại tồn kho;
  fork
    |Nhân Viên|
    :Thông báo nhân viên;
  end fork
  stop
else (Chấp nhận)
  :Đặt ngày giao dự kiến;
  |Hệ Thống|
  :Trạng thái: Chờ đối tác xác nhận (TT_CHO_DOI_TAC_XAC_NHAN = 2);
  :Trạng thái: Đã xác nhận (TT_DA_XAC_NHAN = 3);
  :Trạng thái: Đang xử lý (TT_DANG_XU_LY = 4);
endif

|Hệ Thống|
if (Xử lý đơn hàng?) then (Gặp sự cố)
  :Trạng thái: Gặp sự cố (TT_GAP_SU_CO = 11);
  fork
    |Nhân Viên|
    :Báo cáo sự cố;
  end fork
  if (Giải quyết sự cố?) then (Hủy đơn)
    :Trạng thái: Đã hủy (TT_DA_HUY = 7);
    :Cộng lại tồn kho;
    stop
  else (Tiếp tục)
    :Trạng thái: Đang xử lý (TT_DANG_XU_LY = 4);
  endif
else (Hoàn thành xử lý)
  :Trạng thái: Đã giao (TT_DA_GIAO = 9);
  :Trạng thái: Chờ thanh toán (TT_CHO_THANH_TOAN = 5);
endif

if (Thanh toán?) then (Chưa thanh toán)
  :Chờ thanh toán;
  detach
else (Đã thanh toán)
  :Trạng thái: Đã thanh toán (TT_DA_THANH_TOAN = 10);
  :Trạng thái: Hoàn thành (TT_HOAN_THANH = 6);
  fork
    |Nhân Viên|
    :Tạo hợp đồng;
    :Tạo hóa đơn;
    :Tạo công nợ;
  end fork
endif

stop

legend right
  |**Trạng thái đơn hàng**|
  |TT_MOI_TAO = 1: Mới tạo|
  |TT_CHO_DOI_TAC_XAC_NHAN = 2: Chờ đối tác xác nhận|
  |TT_DA_XAC_NHAN = 3: Đã xác nhận|
  |TT_DANG_XU_LY = 4: Đang xử lý|
  |TT_CHO_THANH_TOAN = 5: Chờ thanh toán|
  |TT_HOAN_THANH = 6: Hoàn thành|
  |TT_DA_HUY = 7: Đã hủy|
  |TT_DOI_TAC_TU_CHOI = 8: Đối tác từ chối|
  |TT_DA_GIAO = 9: Đã giao|
  |TT_DA_THANH_TOAN = 10: Đã thanh toán|
  |TT_GAP_SU_CO = 11: Gặp sự cố|
endlegend

@enduml
```

## Mô Tả Quy Trình

### **Các Trạng Thái Đơn Hàng**

| Trạng Thái | Mã | Mô Tả |
|-----------|----|-------|
| Mới tạo | 1 | Đơn hàng vừa được tạo bởi nhân viên |
| Chờ đối tác xác nhận | 2 | Đã gửi thông báo cho đối tác, chờ xử lý |
| Đã xác nhận | 3 | Đối tác đã chấp nhận và đặt ngày giao |
| Đang xử lý | 4 | Đang thực hiện đơn hàng |
| Chờ thanh toán | 5 | Đã giao hàng, chờ khách thanh toán |
| Hoàn thành | 6 | Đơn hàng hoàn thành 100% |
| Đã hủy | 7 | Đơn hàng bị hủy (do sự cố hoặc khách yêu cầu) |
| Đối tác từ chối | 8 | Đối tác từ chối đơn hàng |
| Đã giao | 9 | Đã giao hàng cho khách |
| Đã thanh toán | 10 | Khách đã thanh toán |
| Gặp sự cố | 11 | Có sự cố xảy ra trong quá trình xử lý |

### **Luồng Chính (Happy Path)**

1. **Nhân viên** tạo đơn hàng → Kiểm tra tồn kho
2. **Hệ thống** set trạng thái "Mới tạo" → Gửi thông báo đối tác
3. **Đối tác** chấp nhận → Đặt ngày giao dự kiến
4. **Hệ thống** cập nhật trạng thái: "Chờ xác nhận" → "Đã xác nhận" → "Đang xử lý"
5. **Hệ thống** hoàn thành xử lý → "Đã giao" → "Chờ thanh toán"
6. **Khách** thanh toán → "Đã thanh toán" → "Hoàn thành"
7. **Nhân viên** tạo: Hợp đồng → Hóa đơn → Công nợ

### **Luồng Exception**

**Đối tác từ chối:**
- Đối tác từ chối → "Đối tác từ chối" → Cộng lại tồn kho → Thông báo nhân viên → Kết thúc

**Gặp sự cố:**
- Gặp sự cố → "Gặp sự cố" → Báo cáo → Giải quyết
  - Nếu hủy: "Đã hủy" → Cộng lại tồn kho → Kết thúc
  - Nếu tiếp tục: Quay lại "Đang xử lý"

**Hết hàng:**
- Kiểm tra tồn kho → Hết hàng → Thông báo → Kết thúc

### **Các Actor Trong Quy Trình**

1. **Nhân viên:** Tạo đơn, xử lý sự cố, tạo hợp đồng/hóa đơn/công nợ
2. **Đối tác:** Xác nhận đơn hàng, đặt ngày giao
3. **Hệ thống:** Quản lý trạng thái, kiểm tra tồn kho, xử lý thanh toán
4. **Khách hàng:** Thanh toán (ngầm định)
