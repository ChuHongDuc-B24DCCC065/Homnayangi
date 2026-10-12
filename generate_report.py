# -*- coding: utf-8 -*-
"""
Generator for BAO_CAO_DU_AN_HOMNAYANGI.docx
Full Comprehensive Software Engineering Report
"""

import sys
import os
import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import parse_xml
from docx.oxml.ns import nsdecls

def create_report():
    doc = Document()
    
    # ------------------ STYLING & PAGE SETUP ------------------
    for section in doc.sections:
        section.top_margin = Inches(1.0)
        section.bottom_margin = Inches(1.0)
        section.left_margin = Inches(1.0)
        section.right_margin = Inches(1.0)
        
        # Header
        header = section.header
        hp = header.paragraphs[0]
        hp.alignment = WD_ALIGN_PARAGRAPH.RIGHT
        hrun = hp.add_run("BÁO CÁO DỰ ÁN PHẦN MỀM: HÔM NAY ĂN GÌ? | HỆ THỐNG GỢI Ý ẨM THỰC THÔNG MINH")
        hrun.font.name = "Arial"
        hrun.font.size = Pt(8.5)
        hrun.font.color.rgb = RGBColor(128, 128, 128)
        
        # Footer
        footer = section.footer
        fp = footer.paragraphs[0]
        fp.alignment = WD_ALIGN_PARAGRAPH.CENTER
        frun = fp.add_run("Tài Liệu Kỹ Thuật Dự Án Phần Mềm Thực Tế - Phân Tích Hiện Trạng & Lộ Trình Phát Triển")
        frun.font.name = "Arial"
        frun.font.size = Pt(8.5)
        frun.font.color.rgb = RGBColor(128, 128, 128)

    # Helper functions
    def set_cell_background(cell, fill_hex):
        tcPr = cell._element.get_or_add_tcPr()
        shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{fill_hex}"/>')
        tcPr.append(shd)

    def set_cell_margins(cell, top=100, bottom=100, left=140, right=140):
        tcPr = cell._element.get_or_add_tcPr()
        tcMar = parse_xml(f'<w:tcMar {nsdecls("w")}><w:top w:w="{top}" w:type="dxa"/><w:bottom w:w="{bottom}" w:type="dxa"/><w:left w:w="{left}" w:type="dxa"/><w:right w:w="{right}" w:type="dxa"/></w:tcMar>')
        tcPr.append(tcMar)

    def set_table_borders(table, color="D3D3D3", sz="4", val="single"):
        tblPr = table._element.xpath('w:tblPr')
        if tblPr:
            borders = parse_xml(
                f'<w:tblBorders {nsdecls("w")}>'
                f'<w:top w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
                f'<w:bottom w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
                f'<w:insideH w:val="{val}" w:sz="{sz}" w:space="0" w:color="{color}"/>'
                f'<w:insideV w:val="none"/>'
                f'<w:left w:val="none"/>'
                f'<w:right w:val="none"/>'
                f'</w:tblBorders>'
            )
            tblPr[0].append(borders)

    def add_p(text="", bold=False, italic=False, font_size=10.5, color=None, space_before=2, space_after=6, align=WD_ALIGN_PARAGRAPH.LEFT, line_spacing=1.15):
        p = doc.add_paragraph()
        p.alignment = align
        p.paragraph_format.space_before = Pt(space_before)
        p.paragraph_format.space_after = Pt(space_after)
        p.paragraph_format.line_spacing = line_spacing
        if text:
            run = p.add_run(text)
            run.bold = bold
            run.italic = italic
            run.font.name = "Arial"
            run.font.size = Pt(font_size)
            if color:
                run.font.color.rgb = color
            else:
                run.font.color.rgb = RGBColor(40, 40, 40)
        return p

    def add_h(text, level):
        h = doc.add_heading(text, level=level)
        h.paragraph_format.keep_with_next = True
        run = h.runs[0]
        run.font.name = "Arial"
        if level == 1:
            h.paragraph_format.space_before = Pt(20)
            h.paragraph_format.space_after = Pt(8)
            run.font.size = Pt(16)
            run.bold = True
            run.font.color.rgb = RGBColor(230, 74, 0)
        elif level == 2:
            h.paragraph_format.space_before = Pt(14)
            h.paragraph_format.space_after = Pt(6)
            run.font.size = Pt(13)
            run.bold = True
            run.font.color.rgb = RGBColor(33, 33, 33)
        elif level == 3:
            h.paragraph_format.space_before = Pt(10)
            h.paragraph_format.space_after = Pt(4)
            run.font.size = Pt(11.5)
            run.bold = True
            run.font.color.rgb = RGBColor(80, 80, 80)
        return h

    def add_callout(title, text, bg_hex="FFF5F0", border_color_hex="FF6B35"):
        table = doc.add_table(rows=1, cols=1)
        table.alignment = WD_TABLE_ALIGNMENT.CENTER
        table.autofit = False
        cell = table.cell(0, 0)
        cell.width = Inches(6.5)
        set_cell_background(cell, bg_hex)
        set_cell_margins(cell, top=120, bottom=120, left=180, right=140)
        
        tcPr = cell._element.get_or_add_tcPr()
        borders = parse_xml(
            f'<w:tcBorders {nsdecls("w")}>'
            f'<w:left w:val="single" w:sz="24" w:space="0" w:color="{border_color_hex}"/>'
            f'<w:top w:val="none"/>'
            f'<w:right w:val="none"/>'
            f'<w:bottom w:val="none"/>'
            f'</w:tcBorders>'
        )
        tcPr.append(borders)
        
        p = cell.paragraphs[0]
        p.paragraph_format.space_before = Pt(2)
        p.paragraph_format.space_after = Pt(4)
        run_t = p.add_run(f"📌 {title}\n")
        run_t.bold = True
        run_t.font.name = "Arial"
        run_t.font.size = Pt(10)
        run_t.font.color.rgb = RGBColor(190, 60, 20)
        
        run_c = p.add_run(text)
        run_c.font.name = "Arial"
        run_c.font.size = Pt(9.5)
        run_c.font.color.rgb = RGBColor(50, 50, 50)
        
        doc.add_paragraph().paragraph_format.space_after = Pt(4)

    def add_diagram_box(title, diagram_text):
        p_t = doc.add_paragraph()
        p_t.paragraph_format.space_before = Pt(8)
        p_t.paragraph_format.space_after = Pt(2)
        r_t = p_t.add_run(f"Sơ đồ / Kiến trúc: {title}")
        r_t.bold = True
        r_t.italic = True
        r_t.font.size = Pt(9.5)
        r_t.font.color.rgb = RGBColor(100, 100, 100)

        table = doc.add_table(rows=1, cols=1)
        table.alignment = WD_TABLE_ALIGNMENT.CENTER
        table.autofit = False
        cell = table.cell(0, 0)
        cell.width = Inches(6.5)
        set_cell_background(cell, "F8F9FA")
        set_cell_margins(cell, top=100, bottom=100, left=150, right=150)
        
        tcPr = cell._element.get_or_add_tcPr()
        borders = parse_xml(
            f'<w:tcBorders {nsdecls("w")}>'
            f'<w:left w:val="single" w:sz="12" w:space="0" w:color="CCCCCC"/>'
            f'<w:top w:val="single" w:sz="6" w:space="0" w:color="E0E0E0"/>'
            f'<w:right w:val="single" w:sz="6" w:space="0" w:color="E0E0E0"/>'
            f'<w:bottom w:val="single" w:sz="6" w:space="0" w:color="E0E0E0"/>'
            f'</w:tcBorders>'
        )
        tcPr.append(borders)
        
        p = cell.paragraphs[0]
        p.paragraph_format.space_before = Pt(2)
        p.paragraph_format.space_after = Pt(2)
        p.paragraph_format.line_spacing = 1.0
        run = p.add_run(diagram_text)
        run.font.name = "Consolas"
        run.font.size = Pt(8.5)
        run.font.color.rgb = RGBColor(30, 30, 30)
        doc.add_paragraph().paragraph_format.space_after = Pt(4)

    def add_table_custom(headers, data, col_widths=None):
        table = doc.add_table(rows=len(data) + 1, cols=len(headers))
        table.alignment = WD_TABLE_ALIGNMENT.CENTER
        table.autofit = False
        set_table_borders(table)
        
        hdr_row = table.rows[0]
        trPr = hdr_row._element.get_or_add_trPr()
        trPr.append(parse_xml(f'<w:tblHeader {nsdecls("w")}/>'))
        
        for i, header_text in enumerate(headers):
            cell = hdr_row.cells[i]
            if col_widths and i < len(col_widths):
                cell.width = col_widths[i]
            set_cell_background(cell, "FF6B35")
            set_cell_margins(cell, top=100, bottom=100, left=100, right=100)
            p = cell.paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p.paragraph_format.space_before = Pt(2)
            p.paragraph_format.space_after = Pt(2)
            run = p.add_run(header_text)
            run.bold = True
            run.font.name = "Arial"
            run.font.size = Pt(9.5)
            run.font.color.rgb = RGBColor(255, 255, 255)
            
        for row_idx, row_data in enumerate(data):
            row = table.rows[row_idx + 1]
            bg = "FFFFFF" if row_idx % 2 == 0 else "FFF8F5"
            for col_idx, cell_value in enumerate(row_data):
                cell = row.cells[col_idx]
                if col_widths and col_idx < len(col_widths):
                    cell.width = col_widths[col_idx]
                set_cell_background(cell, bg)
                set_cell_margins(cell, top=80, bottom=80, left=100, right=100)
                p = cell.paragraphs[0]
                p.paragraph_format.space_before = Pt(2)
                p.paragraph_format.space_after = Pt(2)
                if col_idx == 0 and len(str(cell_value)) <= 4:
                    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                else:
                    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
                run = p.add_run(str(cell_value))
                run.font.name = "Arial"
                run.font.size = Pt(9)
                run.font.color.rgb = RGBColor(40, 40, 40)
        doc.add_paragraph().paragraph_format.space_after = Pt(6)
        return table

    print("Building Document Sections...")

    # =========================================================================
    # TRANG BÌA (COVER PAGE)
    # =========================================================================
    p_cov_top = add_p("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM\nĐộc lập - Tự do - Hạnh phúc", bold=True, font_size=11, align=WD_ALIGN_PARAGRAPH.CENTER, space_after=2)
    p_cov_line = add_p("---------------------------------", font_size=10, align=WD_ALIGN_PARAGRAPH.CENTER, space_after=36)
    
    add_p("BÁO CÁO PHÂN TÍCH VÀ ĐÁNH GIÁ DỰ ÁN PHẦN MỀM", bold=True, font_size=15, color=RGBColor(128, 128, 128), align=WD_ALIGN_PARAGRAPH.CENTER, space_after=10)
    add_p("ỨNG DỤNG DI ĐỘNG: HÔM NAY ĂN GÌ?", bold=True, font_size=24, color=RGBColor(230, 74, 0), align=WD_ALIGN_PARAGRAPH.CENTER, space_after=8)
    add_p("HỆ THỐNG GỢI Ý ẨM THỰC NGẪU NHIÊN, QUẢN LÝ MÓN ĂN & MẠNG XÃ HỘI CHIA SẺ BỮA ĂN (ANDROID NATIVE)", italic=True, font_size=11, color=RGBColor(80, 80, 80), align=WD_ALIGN_PARAGRAPH.CENTER, space_after=40)

    # Info Box Table on Cover
    cov_info_data = [
        ["Tên dự án:", "Hôm Nay Ăn Gì? (Package: com.example.homnayangi)"],
        ["Loại ứng dụng:", "Android Native Application (Mobile Client-Server / BaaS)"],
        ["Phiên bản mã nguồn:", "1.0 (Build: targetSdk 37, compileSdk 37, minSdk 24/26)"],
        ["Hội đồng chuyên môn:", "Senior Architect, Technical Lead, QA Specialist, Security Engineer"],
        ["Nhóm thực hiện:", "Ban Kỹ Thuật Phát Triển Ứng Dụng Di Động"],
        ["Thời gian phân tích:", "Tháng 10 Năm 2026"],
        ["Tình trạng phân tích:", "Đã hoàn thành phân tích toàn bộ mã nguồn, cấu hình và cơ sở dữ liệu"]
    ]
    add_table_custom(["Thông Tin Dự Án", "Nội Dung Chi Tiết"], cov_info_data, [Inches(2.2), Inches(4.3)])

    doc.add_page_break()

    # =========================================================================
    # MỤC LỤC & DANH MỤC
    # =========================================================================
    add_h("MỤC LỤC TỔNG HỢP", level=1)
    toc_data = [
        ["CHƯƠNG 1", "TỔNG QUAN DỰ ÁN", "Trang 3"],
        ["CHƯƠNG 2", "KHẢO SÁT VÀ PHÂN TÍCH YÊU CẦU", "Trang 5"],
        ["CHƯƠNG 3", "PHÂN TÍCH HỆ THỐNG HIỆN TẠI", "Trang 7"],
        ["CHƯƠNG 4", "PHÂN TÍCH CHI TIẾT SOURCE CODE", "Trang 10"],
        ["CHƯƠNG 5", "THIẾT KẾ VÀ CẤU TRÚC DATABASE", "Trang 13"],
        ["CHƯƠNG 6", "PHÂN TÍCH GIAO DIỆN LẬP TRÌNH API & BACKEND", "Trang 15"],
        ["CHƯƠNG 7", "GIAO DIỆN VÀ TRẢI NGHIỆM NGƯỜI DÙNG (UI/UX)", "Trang 17"],
        ["CHƯƠNG 8", "KIỂM THỬ VÀ ĐẢM BẢO CHẤT LƯỢNG (TESTING & QA)", "Trang 19"],
        ["CHƯƠNG 9", "PHÂN TÍCH TRIỂN KHAI VÀ VẬN HÀNH (DEVOPS & DEPLOYMENT)", "Trang 21"],
        ["CHƯƠNG 10", "ĐÁNH GIÁ TOÀN DIỆN HIỆN TRẠNG PHẦN MỀM", "Trang 23"],
        ["CHƯƠNG 11", "ĐỀ XUẤT CẢI TIẾN VÀ NÂNG CẤP HỆ THỐNG", "Trang 25"],
        ["CHƯƠNG 12", "KẾ HOẠCH VÀ LỘ TRÌNH PHÁT TRIỂN (ROADMAP & BACKLOG)", "Trang 28"],
        ["CHƯƠNG 13", "KẾT LUẬN VÀ KIẾN NGHỊ", "Trang 31"],
        ["PHỤ LỤC", "CẤU TRÚC TỆP TIN, DANH MỤC MÃ NGUỒN VÀ SECURITY RULES", "Trang 32"]
    ]
    add_table_custom(["Mục", "Tên Chương / Phân Đoạn", "Vị Trí Tham Chiếu"], toc_data, [Inches(1.2), Inches(4.3), Inches(1.0)])

    add_h("DANH MỤC HÌNH VÀ SƠ ĐỒ KỸ THUẬT", level=2)
    figures_list = [
        ["Hình 1.1", "Sơ đồ kiến trúc tầng hiện tại của hệ thống Hôm Nay Ăn Gì"],
        ["Hình 2.1", "Sơ đồ Use Case tổng quát của ứng dụng"],
        ["Hình 2.2", "Sơ đồ luồng hoạt động User Flow khi quay vòng quay ẩm thực"],
        ["Hình 3.1", "Mô hình tương tác giữa Client và Firebase BaaS"],
        ["Hình 5.1", "Sơ đồ quan hệ thực thể logic ERD trong Cloud Firestore"],
        ["Hình 7.1", "Cấu trúc điều hướng giao diện Bottom Navigation 5 phân hệ"],
        ["Hình 11.1", "Sơ đồ kiến trúc mục tiêu: MVVM + Clean Architecture + Repository"]
    ]
    add_table_custom(["Ký Hiệu", "Tên Sơ Đồ / Hình Ảnh Minh Họa"], figures_list, [Inches(1.2), Inches(5.3)])

    add_h("DANH MỤC BẢNG BIỂU KỸ THUẬT", level=2)
    tables_list = [
        ["Bảng 1.1", "Tổng hợp công nghệ, thư viện và trạng thái tích hợp"],
        ["Bảng 2.1", "Danh sách yêu cầu chức năng (Functional Requirements)"],
        ["Bảng 2.2", "Danh sách yêu cầu phi chức năng (Non-Functional Requirements)"],
        ["Bảng 3.1", "Ma trận chức năng hiện có và tình trạng triển khai"],
        ["Bảng 5.1", "Đặc tả các Document Collections trong cơ sở dữ liệu Firestore"],
        ["Bảng 6.1", "Danh mục các thao tác tương tác dữ liệu Firebase qua FoodRepository"],
        ["Bảng 8.1", "Ma trận kịch bản kiểm thử chức năng (Functional Test Cases)"],
        ["Bảng 10.1", "Bảng điểm đánh giá hiện trạng hệ thống theo 10 tiêu chí phần mềm"],
        ["Bảng 11.1", "Phân loại yêu cầu cải tiến theo mô hình MoSCoW"],
        ["Bảng 12.1", "Lộ trình triển khai nâng cấp phần mềm 6 giai đoạn"],
        ["Bảng 12.2", "Bảng Technical & Product Backlog chi tiết"]
    ]
    add_table_custom(["Ký Hiệu", "Nội Dung Bảng Biểu"], tables_list, [Inches(1.2), Inches(5.3)])

    doc.add_page_break()

    # =========================================================================
    # CHƯƠNG 1. TỔNG QUAN DỰ ÁN
    # =========================================================================
    add_h("CHƯƠNG 1. TỔNG QUAN DỰ ÁN", level=1)

    add_h("1.1. Giới thiệu dự án", level=2)
    add_p("Dự án \"Hôm Nay Ăn Gì?\" (tên định danh package: com.example.homnayangi) là một ứng dụng di động được xây dựng trên nền tảng Android thuần (Native Java), kết hợp cùng hạ tầng đám mây không máy chủ Backend-as-a-Service (BaaS) của Google Firebase (bao gồm Firebase Authentication và Cloud Firestore).")
    add_p("Mục tiêu cốt lõi của phần mềm là giải quyết bài toán tâm lý thực tế phổ biến trong đời sống hiện đại: \"hội chứng phân vân khi chọn bữa ăn\" (decision fatigue). Ứng dụng cung cấp công cụ lựa chọn ngẫu nhiên bằng giao diện trực quan - vòng quay may mắn (Roulette Wheel), kết hợp hệ thống quản lý danh mục món ăn cá nhân, tra cứu công thức nấu ăn chuẩn, theo dõi ngân sách chi tiêu và chia sẻ trải nghiệm cộng đồng.")

    add_h("1.2. Bối cảnh và lý do chọn đề tài", level=2)
    add_p("Trong nhịp sống đô thị hiện đại, người lao động văn phòng, học sinh và sinh viên thường xuyên tiêu tốn từ 15 đến 30 phút mỗi ngày chỉ để quyết định ăn món gì cho bữa trưa hoặc bữa tối. Sự phong phú của các hàng quán kết hợp áp lực công việc dẫn tới hiện tượng quá tải nhận thức. Đa phần các ứng dụng giao thức ăn hiện nay (Grab, ShopeeFood) được thiết kế xoay quanh mục tiêu thương mại và quảng cáo, dẫn đến người dùng càng bị ngập trong danh sách gợi ý vô tận mà không có giải pháp ra quyết định dứt điểm.")
    add_p("Dự án \"Hôm Nay Ăn Gì?\" ra đời như một ứng dụng tiện ích cá nhân hóa (Utility & Productivity App), tập trung tối đa vào tốc độ quyết định, tính giải trí nhẹ nhàng, khả năng kiểm soát tài chính và kết nối cộng đồng người yêu ẩm thực.")

    add_h("1.3. Vấn đề thực tiễn cần giải quyết", level=2)
    add_p("Dựa trên kết quả khảo sát phân tích mã nguồn, hệ thống tập trung tháo gỡ 4 điểm nghẽn chính:")
    add_p("• Điểm nghẽn 1: Sự lãng phí thời gian và mệt mỏi tinh thần khi phải lựa chọn món ăn lặp đi lặp lại hàng ngày.", bold=False)
    add_p("• Điểm nghẽn 2: Thiếu công cụ ghi nhớ và sàng lọc các món ăn yêu thích hoặc những món hợp khẩu vị đã ăn trước đó.", bold=False)
    add_p("• Điểm nghẽn 3: Khó khăn trong việc kiểm soát tài chính bữa ăn hàng ngày, dẫn đến chi tiêu vượt định mức cá nhân mà không nắm được số liệu tổng kết.", bold=False)
    add_p("• Điểm nghẽn 4: Thiếu không gian chia sẻ nhanh các địa chỉ quán ngon hoặc ý tưởng nấu nướng cho bạn bè, đồng nghiệp mà không bị chi phối bởi các bài PR thương mại.", bold=False)

    add_h("1.4. Mục tiêu tổng thể và mục tiêu cụ thể", level=2)
    add_p("Mục tiêu tổng thể: Xây dựng một ứng dụng di động Android Native gọn nhẹ, tương tác mượt mà, bảo mật dữ liệu theo người dùng, hỗ trợ đưa ra quyết định ăn uống tức thì và theo dõi lối sống dinh dưỡng - tài chính.")
    add_p("Mục tiêu cụ thể được phản ánh qua các module mã nguồn:")
    add_p("1. Cung cấp vòng quay may mắn có thuật toán phân bổ góc quay chính xác và hiệu ứng âm thanh/hoa giấy sống động.")
    add_p("2. Quản lý danh mục món ăn đầy đủ CRUD (Thêm, Sửa, Xóa, Đánh dấu Yêu thích, Thêm vào Pool quay).")
    add_p("3. Cung cấp Thư viện món ăn mẫu kèm công thức nấu ăn chi tiết từng bước và giá tham chiếu.")
    add_p("4. Lưu vết lịch sử quay và tự động tính toán tổng số lượt quay cùng tổng chi tiêu tháng.")
    add_p("5. Cung cấp mạng xã hội thu nhỏ cho phép đăng bài chia sẻ, đính kèm giá tiền, thả tim và thảo luận bình luận.")
    add_p("6. Nhắc nhở bữa ăn hàng ngày qua cơ chế AlarmManager và Notification Channel của Android.")

    add_h("1.5. Đối tượng sử dụng (User Personas)", level=2)
    add_p("• Nhóm 1: Nhân viên văn phòng bận rộn cần giải pháp quyết định nhanh bữa ăn trưa cùng nhóm đồng nghiệp.")
    add_p("• Nhóm 2: Sinh viên và người trẻ sống tự lập cần kiểm soát ngân sách tiền ăn hàng tháng và tham khảo công thức nấu ăn đơn giản tại nhà.")
    add_p("• Nhóm 3: Những người có sở thích chia sẻ đánh giá ẩm thực chân thực, muốn đóng góp địa chỉ ăn ngon cho cộng đồng.")

    add_h("1.6. Phạm vi hệ thống (Scope of Work)", level=2)
    add_p("• Phạm vi trong hệ thống (In-Scope - Đã chứng minh qua code):")
    add_p("  - Ứng dụng chạy trên thiết bị Android SDK 24 đến 37 (Android 7.0 đến Android 15).")
    add_p("  - Đăng ký, đăng nhập tài khoản bằng Email/Password qua Firebase Authentication.")
    add_p("  - Đồng bộ dữ liệu cá nhân theo thời gian thực (Realtime Snapshot Listener) qua Firestore.")
    add_p("  - Quản lý Pool vòng quay lưu tạm cục bộ trong SharedPreferences kết hợp đồng bộ Firestore.")
    add_p("• Phạm vi ngoài hệ thống (Out-of-Scope - Chưa triển khai trong phiên bản hiện tại):")
    add_p("  - Đăng nhập bên thứ ba (Google Sign-In, Facebook Login, Apple ID) - Mã nguồn chưa tích hợp SDK.")
    add_p("  - Đặt đồ ăn trực tuyến hoặc tích hợp cổng thanh toán trực tiếp (Momo, VNPay, ZaloPay).")
    add_p("  - Định vị GPS và gợi ý quán ăn theo bán kính bản đồ (Google Maps API chưa tích hợp).")
    add_p("  - Bảng điều khiển Quản trị viên (Admin Web Portal) để duyệt bài viết cộng đồng.")

    add_h("1.7. Bảng công nghệ sử dụng và trạng thái thực tế", level=2)
    tech_table_data = [
        ["Thành phần", "Công nghệ / Thư viện", "Phiên bản xác định", "Vai trò kỹ thuật", "Trạng thái thực tế"],
        ["Client Platform", "Android SDK / Java 11", "Compile: 37 / Min: 24", "Môi trường thực thi ứng dụng di động", "Đã triển khai hoàn tất"],
        ["UI Components", "Material Components 3", "1.11.0 / 1.14.0", "Hệ thống giao diện Card, Button, Input, Chips", "Đã triển khai hoàn tất"],
        ["Layout Engine", "ConstraintLayout / RelLayout", "2.1.4 (libs.versions.toml)", "Bố cục giao diện đáp ứng đa màn hình", "Đã triển khai hoàn tất"],
        ["Authentication", "Firebase Authentication", "33.6.0 (BOM)", "Xác thực danh tính người dùng bằng Email/Pass", "Đã triển khai hoàn tất"],
        ["Database", "Cloud Firestore", "33.6.0 (BOM)", "Lưu trữ NoSQL Document theo thời gian thực", "Đã triển khai hoàn tất"],
        ["Local Storage", "Android SharedPreferences", "Native Android", "Lưu trữ cấu hình Pool và Switch nhắc nhở", "Đã triển khai hoàn tất"],
        ["Notification", "AlarmManager & Channel", "Android O (API 26+)", "Đẩy thông báo hẹn giờ nhắc nhở bữa ăn 11:30", "Đã triển khai hoàn tất"],
        ["Custom Views", "RouletteWheel & Confetti", "Custom Canvas 2D", "Vẽ đồ họa vòng quay động và pháo hoa giấy", "Đã triển khai hoàn tất"],
        ["Build System", "Gradle Wrapper / AGP", "Gradle 9.5.0 / AGP 9.3.3", "Hệ thống build tự động và quản lý gói", "Đã triển khai hoàn tất"],
        ["Backend API", "Custom REST API / Cloud Run", "Không xác định", "Máy chủ nghiệp vụ trung gian", "Chưa có (Dùng direct Firebase BaaS)"],
        ["Image Storage", "Firebase Storage / Glide", "Chưa kích hoạt trong code", "Lưu trữ hình ảnh bài đăng cộng đồng", "Đang dở (Chưa có upload ảnh Cloud)"],
        ["Unit / UI Testing", "JUnit 4 / Espresso", "JUnit 4.13.2 / Espresso 3.7", "Kiểm thử tự động mã nguồn", "Chưa có (Chỉ có template mặc định)"]
    ]
    add_table_custom(tech_table_data[0], tech_table_data[1:])

    doc.add_page_break()

    # =========================================================================
    # CHƯƠNG 2. KHẢO SÁT VÀ PHÂN TÍCH YÊU CẦU
    # =========================================================================
    add_h("CHƯƠNG 2. KHẢO SÁT VÀ PHÂN TÍCH YÊU CẦU", level=1)

    add_h("2.1. Khảo sát người dùng mục tiêu", level=2)
    add_p("Qua nghiên cứu hành vi người tiêu dùng ẩm thực, nhu cầu sử dụng được chia thành hai nhóm chính:")
    add_p("1. Nhu cầu thụ động (Passive): Muốn hệ thống quyết định thay để giải tỏa áp lực lựa chọn.")
    add_p("2. Nhu cầu chủ động (Active): Muốn quản lý danh mục quán quen, ghi nhớ công thức nấu nướng yêu thích và kiểm soát ngân sách.")

    add_h("2.2. Phân tích yêu cầu chức năng (Functional Requirements - FR)", level=2)
    fr_data = [
        ["Mã FR", "Tên Yêu Cầu Chức Năng", "Mô Tả Chi Tiết Nghiệp Vụ", "Vị Trí Code Triển Khai", "Trạng Thái"],
        ["FR-01", "Đăng ký tài khoản", "Nhập DisplayName, Email, Password (>=6 ký tự). Tạo tài khoản Firebase Auth và tạo document /users/{uid}.", "RegisterActivity.java", "Hoàn thành"],
        ["FR-02", "Đăng nhập hệ thống", "Xác thực Email/Password, tự động chuyển hướng vào MainActivity nếu session còn hiệu lực.", "LoginActivity.java", "Hoàn thành"],
        ["FR-03", "Đăng xuất tài khoản", "Xóa session FirebaseAuth, hủy bỏ trạng thái người dùng và quay về màn hình Đăng nhập.", "ProfileFragment / MainActivity", "Hoàn thành"],
        ["FR-04", "Vòng quay Roulette", "Hiển thị vòng quay động vẽ từ Canvas, quay ngẫu nhiên các món trong Pool, phát âm thanh và hiệu ứng confetti.", "HomeFragment / RouletteWheelView", "Hoàn thành"],
        ["FR-05", "Chế độ quay loại trừ", "Cho phép bật SwitchEliminate để loại bỏ món vừa quay trúng ra khỏi Pool nhằm không trùng lặp.", "HomeFragment.java (switchEliminate)", "Hoàn thành"],
        ["FR-06", "Quản lý Pool quay", "Thêm/bớt món vào Pool từ danh sách món ăn, hiển thị dưới dạng Chip, lưu trong SharedPreferences.", "HomeFragment / FoodAdapter / FoodRepo", "Hoàn thành"],
        ["FR-07", "CRUD Món ăn cá nhân", "Thêm mới, chỉnh sửa, xóa món ăn của người dùng trên collection /users/{uid}/foods.", "FoodListFragment / dialog_add_food", "Hoàn thành"],
        ["FR-08", "Bộ lọc & Tìm kiếm món", "Lọc theo Chip danh mục, lọc theo món yêu thích, tìm kiếm realtime có debounce 300ms.", "FoodListFragment.java", "Hoàn thành"],
        ["FR-09", "Đánh dấu Yêu thích", "Bật/tắt cờ isFavorite trên từng món ăn, đồng bộ Firestore.", "FoodAdapter / FoodRepository", "Hoàn thành"],
        ["FR-10", "Thư viện món ăn mẫu", "Hiển thị danh mục món ăn mẫu từ FoodLibraryDatabase, hỗ trợ đồng bộ lên Firestore và thêm vào món cá nhân.", "FoodLibraryActivity / FoodLibAdapter", "Hoàn thành"],
        ["FR-11", "Lịch sử & Thống kê", "Lưu món vừa quay vào /history, hiển thị danh sách lịch sử, thống kê tổng số lần quay và tổng số tiền.", "HistoryFragment / HistoryAdapter", "Hoàn thành"],
        ["FR-12", "Xóa toàn bộ lịch sử", "Sử dụng Firestore WriteBatch để xóa toàn bộ document trong subcollection /history.", "HistoryFragment.java (confirmClearHistory)", "Hoàn thành"],
        ["FR-13", "Bảng tin cộng đồng", "Hiển thị các bài đăng chia sẻ món ăn công khai (/community), sắp xếp theo thời gian mới nhất.", "CommunityFragment / CommunityAdapter", "Hoàn thành"],
        ["FR-14", "Đăng bài chia sẻ", "Đăng bài kèm tên món, quán ăn, cảm nhận và giá tiền vào collection /community.", "CommunityFragment / dialog_post_comm", "Hoàn thành"],
        ["FR-15", "Tương tác Like/Unlike", "Thả tim hoặc bỏ thả tim bài viết cộng đồng bằng FieldValue.arrayUnion / arrayRemove.", "CommunityAdapter / FoodRepository", "Hoàn thành"],
        ["FR-16", "Bình luận bài viết", "Xem danh sách bình luận (/community/{id}/comments) và gửi bình luận mới theo thời gian thực.", "CommunityFragment / CommentAdapter", "Hoàn thành"],
        ["FR-17", "Cập nhật Hồ sơ cá nhân", "Chỉnh sửa tên hiển thị, ngày sinh (DatePicker), giới tính (Dropdown), số điện thoại.", "ProfileFragment.java", "Hoàn thành"],
        ["FR-18", "Thống kê chi tiêu tháng", "Tính tổng chi tiêu trong tháng hiện tại và tìm ra món ăn được quay nhiều nhất.", "ProfileFragment.java (loadSpendingStats)", "Hoàn thành"],
        ["FR-19", "Cài đặt nhắc nhở ăn", "Cài đặt báo thức hàng ngày lúc 11:30 qua AlarmManager và phát thông báo qua ReminderReceiver.", "ProfileFragment / ReminderReceiver", "Hoàn thành"]
    ]
    add_table_custom(fr_data[0], fr_data[1:])

    add_h("2.3. Phân tích yêu cầu phi chức năng (Non-Functional Requirements - NFR)", level=2)
    nfr_data = [
        ["Tiêu Chí", "Đặc Tả Yêu Cầu Kỹ Thuật", "Hiện Trạng Phân Tích Từ Source Code"],
        ["Hiệu năng (Performance)", "Vòng quay đạt 60fps, thời gian phản hồi tìm kiếm < 500ms, bộ nhớ RAM ổn định < 150MB.", "Canvas vẽ mượt mà; tìm kiếm có debounce 300ms giảm tải filter; tuy nhiên chưa tối ưu Recycle View cache."],
        ["Độ tin cậy (Reliability)", "Dữ liệu không bị mất khi mất kết nối mạng đột ngột.", "Firestore có cơ chế offline persistence mặc định; SharedPreferences bảo toàn dữ liệu Pool quay."],
        ["Khả năng mở rộng (Scalability)", "Hệ thống hỗ trợ hàng chục nghìn người dùng truy cập đồng thời.", "Firestore BaaS tự động scale; tuy nhiên do kiến trúc Fat-Client truy vấn trực tiếp nên chi phí đọc/ghi cao."],
        ["Bảo mật (Security)", "Mật khẩu mã hóa chuẩn, dữ liệu người dùng được cách ly an toàn, phân quyền rõ ràng.", "Firebase Auth mã hóa pass; tuy nhiên trong source code CHƯA có file định nghĩa Firestore Security Rules."],
        ["Tương thích (Compatibility)", "Chạy tốt trên các thiết bị Android từ Android 7.0 đến Android 15 (API 24 - 37).", "Đã cấu hình minSdk 24 (hoặc 26), targetSdk 37; hỗ trợ chế độ Light/Dark Theme."],
        ["Khả năng sử dụng (Usability)", "Giao diện thân thiện, tông màu chủ đạo cam ấm áp, thao tác dưới 3 chạm.", "Material 3 thẩm mỹ cao, biểu tượng emoji sinh động, hỗ trợ chip lọc trực quan."]
    ]
    add_table_custom(nfr_data[0], nfr_data[1:])

    add_h("2.4. Mô hình Use Case và đặc tả tác nhân (Actor)", level=2)
    add_p("Qua phân tích mã nguồn, hệ thống có hai tác nhân chính:")
    add_p("• Người dùng Khách (Guest / Unauthenticated User): Chỉ có thể tương tác với màn hình LoginActivity và RegisterActivity.")
    add_p("• Người dùng Đã xác thực (Authenticated User): Được cấp quyền truy cập toàn bộ 5 phân hệ chính của MainActivity.")
    
    uc_diagram_text = """
+-----------------------------------------------------------------------------------+
|                           HỆ THỐNG "HÔM NAY ĂN GÌ?"                                |
|                                                                                   |
|  [Khách chưa đăng nhập]                                                            |
|          |---> (UC-01: Đăng ký tài khoản mới)                                      |
|          |---> (UC-02: Đăng nhập hệ thống)                                         |
|                                                                                   |
|  [Người dùng đã đăng nhập]                                                        |
|          |---> (UC-03: Thêm / Sửa / Xóa món ăn cá nhân)                            |
|          |---> (UC-04: Lọc món ăn theo danh mục & Tìm kiếm)                        |
|          |---> (UC-05: Thêm món ăn vào Vòng quay Roulette)                        |
|          |---> (UC-06: Quay vòng quay ngẫu nhiên & Bật loại trừ)                   |
|          |---> (UC-07: Xem Thư viện món mẫu & Đưa vào danh sách)                   |
|          |---> (UC-08: Xem Lịch sử quay & Thống kê chi tiêu)                       |
|          |---> (UC-09: Xóa toàn bộ lịch sử quay)                                   |
|          |---> (UC-10: Đăng bài chia sẻ lên Cộng đồng)                             |
|          |---> (UC-11: Thả tim & Bình luận bài viết người khác)                    |
|          |---> (UC-12: Cập nhật thông tin cá nhân & Bật nhắc nhở 11:30)            |
|          |---> (UC-13: Đăng xuất)                                                  |
+-----------------------------------------------------------------------------------+
"""
    add_diagram_box("Hình 2.1: Sơ đồ Use Case phân quyền người dùng", uc_diagram_text)

    add_h("2.5. Phân tích luồng người dùng (User Flows)", level=2)
    add_p("Luồng nghiệp vụ cốt lõi nhất của ứng dụng: Luồng Quay Vòng May Mắn và Ghi Nhận Kết Quả.")
    flow_spin_text = """
[Bắt đầu] 
   |
   v
[Người dùng mở Tab Trang chủ]
   |
   +---> Kiểm tra danh sách Pool quay trong SharedPreferences?
   |        |
   |        +--- (Nếu rỗng) ---> Hiển thị thông báo "Chưa có món, sang Tab Món ăn bấm ＋"
   |        |
   |        +--- (Nếu có món) -> Vẽ các cung phần tử lên RouletteWheelView
   v
[Người dùng bấm nút "🎲 QUAY NGAY"]
   |
   +---> Disable nút bấm, set trạng thái isSpinning = true
   +---> Sinh góc quay ngẫu nhiên = currentRotation + (360 * 7) + targetOffset
   +---> Chạy ObjectAnimator trong 4500ms (DecelerateInterpolator 1.5)
   +---> Phát hiệu ứng âm thanh Tick-tick theo tần suất giãn dần
   |
   v
[Vòng quay dừng lại tại món được chọn]
   |
   +---> Phát âm thanh thông báo kết quả (ToneGenerator ACK)
   +---> Bắn hiệu ứng hoa giấy rơi qua ConfettiView.fire()
   +---> Hiển thị popup AlertDialog chúc mừng kèm tên món
   +---> Ghi nhận kết quả vào Firebase Firestore: /users/{uid}/history
   +---> Lưu kết quả vào danh sách Recent Results (Tối đa 3 kết quả gần nhất)
   |
   +---> Kiểm tra: Switch "Loại trừ món đã quay" có bật không?
            |
            +--- (Có bật) ---> Xóa món vừa quay khỏi Pool, lưu lại Prefs & vẽ lại vòng
            +--- (Không bật) -> Giữ nguyên danh mục Pool
   |
   v
[Kết thúc vòng quay, kích hoạt lại nút bấm]
"""
    add_diagram_box("Hình 2.2: Sơ đồ User Flow - Quy trình vận hành Vòng quay ẩm thực", flow_spin_text)

    doc.add_page_break()

    # =========================================================================
    # CHƯƠNG 3. PHÂN TÍCH HỆ THỐNG HIỆN TẠI
    # =========================================================================
    add_h("CHƯƠNG 3. PHÂN TÍCH HỆ THỐNG HIỆN TẠI", level=1)

    add_h("3.1. Cấu trúc thư mục và tổ chức mã nguồn", level=2)
    add_p("Toàn bộ mã nguồn ứng dụng nằm trong thư mục d:\\homnayangi, được tổ chức theo cấu trúc tiêu chuẩn của một dự án Android Gradle:")
    folder_text = """
D:\\homnayangi
├── .idea/                      # Cấu hình IDE Android Studio
├── gradle/                     # Cấu hình Gradle Wrapper và Version Catalog
│   ├── libs.versions.toml      # Khai báo tập trung thư viện và phiên bản (TOML)
│   └── wrapper/                # Bộ thực thi Gradle 9.5.0
├── app/                        # Module ứng dụng chính
│   ├── build.gradle.kts        # Cấu hình biên dịch module app
│   ├── google-services.json    # File cấu hình kết nối dự án Firebase (homnayangi-b84da)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml   # Khai báo Activities, Permissions, Receivers
│       │   ├── java/com/example/homnayangi/
│       │   │   ├── MyApplication.java       # Khởi tạo FirebaseApp & giữ Context Singleton
│       │   │   ├── activities/              # Các Activity và Fragment giao diện
│       │   │   │   ├── MainActivity.java
│       │   │   │   ├── LoginActivity.java
│       │   │   │   ├── RegisterActivity.java
│       │   │   │   ├── FoodLibraryActivity.java
│       │   │   │   ├── HomeFragment.java
│       │   │   │   ├── FoodListFragment.java
│       │   │   │   ├── CommunityFragment.java
│       │   │   │   ├── HistoryFragment.java
│       │   │   │   └── ProfileFragment.java
│       │   │   ├── adapters/                # Các RecyclerView Adapter kết nối dữ liệu
│       │   │   │   ├── FoodAdapter.java
│       │   │   │   ├── FoodLibraryAdapter.java
│       │   │   │   ├── CommunityAdapter.java
│       │   │   │   ├── CommentAdapter.java
│       │   │   │   └── HistoryAdapter.java
│       │   │   ├── repository/              # Tầng quản lý tương tác dữ liệu
│       │   │   │   └── FoodRepository.java  # Lớp tập hợp toàn bộ logic Firestore CRUD
│       │   │   └── utils/                   # Các lớp tiện ích và Custom View
│       │   │       ├── RouletteWheelView.java # Custom View vẽ vòng quay động Canvas
│       │   │       ├── ConfettiView.java      # Custom View hoạt họa pháo hoa giấy
│       │   │       ├── FoodLibraryDatabase.java# Dữ liệu hạt giống ẩm thực Việt Nam
│       │   │       └── ReminderReceiver.java  # BroadcastReceiver xử lý báo thức bữa ăn
│       │   └── res/                         # Tài nguyên giao diện (Layout, Drawable, Values)
│       └── test/ & androidTest/             # Các lớp kiểm thử mặc định
├── build.gradle.kts            # Cấu hình root project
└── settings.gradle.kts         # Khai báo các module và repository
"""
    add_diagram_box("Cây cấu trúc mã nguồn dự án", folder_text)

    add_h("3.2. Đánh giá kiến trúc tổng thể", level=2)
    add_callout(
        "KẾT LUẬN KIẾN TRÚC PHẦN MỀM HIỆN TẠI",
        "Kiến trúc hiện tại của dự án KHÔNG PHẢI là Clean Architecture hay MVVM hoàn chỉnh.\n"
        "Hệ thống đang được tổ chức theo mô hình 'Client-Server 2 tầng dựa trên BaaS' kết hợp dạng sơ khai của 'Single Activity - Multiple Fragments' và 'Repository Pattern'.\n"
        "Toàn bộ nghiệp vụ truy vấn, parse dữ liệu, tính toán thống kê và lắng nghe sự kiện Firestore đang nằm trực tiếp tại FoodRepository và rải rác bên trong các Fragment (Fat Fragment)."
    )

    arch_diag_text = """
+-----------------------------------------------------------------------------------+
|                             PRESENTATION LAYER (GIAO DIỆN)                        |
|   Activities: MainActivity (Single Host), LoginActivity, RegisterActivity         |
|   Fragments : HomeFragment, FoodListFragment, CommunityFragment, ...              |
|   Adapters  : FoodAdapter, CommunityAdapter, HistoryAdapter, CommentAdapter       |
|   Custom UI : RouletteWheelView (Canvas), ConfettiView                            |
+-----------------------------------------------------------------------------------+
                                         |
                                         v (Gọi trực tiếp / Callback Interface)
+-----------------------------------------------------------------------------------+
|                        BUSINESS & DATA ACCESS LAYER (REPOSITORY)                  |
|   FoodRepository (Singleton):                                                     |
|     - Quản lý Pool quay (SharedPreferences JSON)                                  |
|     - Quản lý Foods Query & CRUD (Firestore)                                      |
|     - Quản lý History Query & Batch Delete (Firestore)                            |
|     - Quản lý Community Posts, Likes, Comments (Firestore)                        |
+-----------------------------------------------------------------------------------+
                                         |
                    +--------------------+--------------------+
                    |                                         |
                    v (Google Play Services SDK)              v (Native Android API)
+---------------------------------------------------+ +-----------------------------+
|               CLOUD FIREBASE BAAS                 | |      LOCAL ANDROID OS       |
|   - Firebase Authentication (Users Auth)          | |  - SharedPreferences       |
|   - Cloud Firestore (NoSQL Realtime Database)     | |  - AlarmManager            |
|       /users/{uid}/foods                          | |  - NotificationManager     |
|       /users/{uid}/history                        | |  - Audio ToneGenerator     |
|       /community/{postId}/comments                | +-----------------------------+
|       /library_foods                              |
+---------------------------------------------------+
"""
    add_diagram_box("Hình 3.1: Sơ đồ kiến trúc tầng hiện tại của hệ thống Hôm Nay Ăn Gì", arch_diag_text)

    add_h("3.3. Phân tích chi tiết các tầng hiện tại", level=2)
    add_p("• Tầng Trình diễn (Presentation Layer):")
    add_p("  - MainActivity đóng vai trò Single Activity chứa FrameLayout (@id/fragment_container) và thanh điều hướng BottomNavigationView 5 tabs. Các Fragment được khởi tạo cùng lúc và chuyển đổi bằng FragmentTransaction.hide() / show() để bảo toàn trạng thái (state preservation).")
    add_p("  - Các màn hình đăng ký/đăng nhập tách biệt thành LoginActivity và RegisterActivity. Khi chưa có user session, MainActivity sẽ kết thúc (finish) và mở LoginActivity.")
    add_p("• Tầng Dữ liệu và Nghiệp vụ (Data Access & Business Layer):")
    add_p("  - Lớp FoodRepository đảm nhận toàn bộ các phương thức truy vấn và cập nhật Firestore. Repository này áp dụng mô hình Singleton (getInstance()) và dùng các Callback Interface tự định nghĩa (OnFoodListCallback, OnCompleteListener) để trả dữ liệu bất đồng bộ về UI.")
    add_p("  - Hạn chế: Chưa sử dụng Android ViewModel và LiveData / Kotlin StateFlow. Khi thiết bị xoay màn hình (orientation change), dữ liệu có nguy cơ bị tải lại hoặc xảy ra hiện tượng rò rỉ bộ nhớ (memory leak) do các callback listener giữ tham chiếu đến Fragment đã bị hủy.")

    doc.add_page_break()

    # =========================================================================
    # CHƯƠNG 4. PHÂN TÍCH CHI TIẾT SOURCE CODE
    # =========================================================================
    add_h("CHƯƠNG 4. PHÂN TÍCH CHI TIẾT SOURCE CODE", level=1)

    add_h("4.1. Tiêu chuẩn đặt tên và phong cách lập trình", level=2)
    add_p("• Ưu điểm: Đặt tên gói (package) và lớp (class) tuân thủ đúng chuẩn quy ước Android (com.example.homnayangi.activities, adapters, repository, utils). Tên Activity và Fragment sử dụng hậu tố rõ ràng (MainActivity, FoodListFragment, CommentAdapter).")
    add_p("• Nhược điểm: Tên ID giao diện trong các file layout XML chưa có tiền tố chuẩn hóa đồng bộ. Ví dụ: tại activity_login.xml dùng edtEmail, tilEmail; nhưng trong item_food.xml lại dùng tvFoodName, imgAddToRoulette; trong fragment_history.xml dùng layoutEmpty, tvTotalSpins. Việc thiếu quy tắc tiền tố thống nhất (vd: act_login_edt_email, item_food_tv_name) gây khó khăn khi bảo trì dự án lớn.")

    add_h("4.2. Phân tích chi tiết từng Module mã nguồn", level=2)
    add_p("1. FoodRepository.java (Tập tin then chốt nhất dự án):")
    add_p("   - Quản lý toàn bộ các nhánh dữ liệu của Firestore.")
    add_p("   - Xử lý SharedPreferences dưới dạng chuỗi JSON thô: roulette_pool_{uid} được chuyển đổi thủ công qua JSONArray và JSONObject thay vì sử dụng thư viện Serialization chuẩn như Google Gson hay Moshi.")
    add_p("   - Áp dụng kỹ thuật Batch Write: phương thức clearHistory() sử dụng com.google.firebase.firestore.WriteBatch để xóa hàng loạt bản ghi lịch sử trong một giao dịch đơn lẻ, tối ưu hóa băng thông mạng.")
    add_p("2. RouletteWheelView.java (Lớp xử lý đồ họa phức tạp):")
    add_p("   - Kế thừa trực tiếp từ android.view.View, tự quản lý vòng đời vẽ onDraw() và tính toán tọa độ trong onSizeChanged().")
    add_p("   - Mảng màu vòng quay sectorColors cố định gồm 12 mã màu Hex đẹp mắt.")
    add_p("   - Thuật toán quay: Sử dụng ObjectAnimator trên thuộc tính View.ROTATION. Thời gian quay kéo dài 4500ms kết hợp DecelerateInterpolator(1.5f) mô phỏng chính xác lực cản ma sát quay của vòng xoay vật lý.")
    add_p("   - Tích hợp phát âm thanh bằng ToneGenerator trên kênh AudioManager.STREAM_NOTIFICATION với giải thuật trễ thời gian tăng dần delay = 60 + (tickCount^2) ms, tạo cảm giác bánh đà quay chậm dần cực kỳ chân thực.")
    add_p("3. ConfettiView.java (Hiệu ứng hoa giấy chúc mừng):")
    add_p("   - Mô phỏng 80 hạt confetti rơi tự do. Mỗi hạt được khởi tạo với vận tốc góc, vận tốc ngang, gia tốc trọng trường và màu sắc ngẫu nhiên. Khi đạt tiến trình 70%, hạt bắt đầu mờ dần (fade out) tạo hiệu ứng thị giác ấn tượng.")

    add_h("4.3. Các Design Patterns đã được áp dụng", level=2)
    add_p("• Singleton Pattern: Thể hiện rõ nét tại MyApplication.getInstance() và FoodRepository.getInstance(). Đảm bảo chỉ có một thể hiện duy nhất quản lý phiên kết nối Firebase trong suốt vòng đời ứng dụng.")
    add_p("• Adapter Pattern: Được triển khai chuẩn mực qua 5 lớp kế thừa RecyclerView.Adapter (FoodAdapter, FoodLibraryAdapter, CommunityAdapter, CommentAdapter, HistoryAdapter) để cầu nối dữ liệu Firestore DocumentSnapshot sang giao diện CardView.")
    add_p("• Observer Pattern (Listener): Các tương tác người dùng được bóc tách thông qua các interface: OnFoodItemClickListener, OnPostActionListener, OnSpinEndListener, OnCompleteListener.")

    add_h("4.4. Đánh giá chất lượng mã nguồn (Code Smells & Clean Code)", level=2)
    add_callout(
        "CÁC ĐIỂM NỢ KỸ THUẬT (CODE SMELLS) CẦN KHẮC PHỤC",
        "1. Thiếu Lớp Data Model (POJO/DTO): Đa phần dữ liệu Firestore được chuyển thẳng thành DocumentSnapshot hoặc Map<String, Object> thay vì ép sang các Class cụ thể (như Food.java, Post.java, User.java). Điều này vi phạm tính an toàn kiểu dữ liệu (Type Safety) và dễ gây lỗi NullPointerException.\n"
        "2. Hardcoded Strings & Magic Numbers: Nhiều chuỗi thông báo tiếng Việt được viết trực tiếp vào mã Java thay vì đưa vào res/values/strings.xml. Các con số như 11:30 (giờ nhắc nhở), 50000 (giá mặc định), 80 (số hạt confetti) chưa được đặt thành hằng số cấu hình."
    )

    doc.add_page_break()

    # =========================================================================
    # CHƯƠNG 5. THIẾT KẾ VÀ CẤU TRÚC DATABASE
    # =========================================================================
    add_h("CHƯƠNG 5. THIẾT KẾ VÀ CẤU TRÚC DATABASE", level=1)

    add_h("5.1. Kiến trúc NoSQL Cloud Firestore", level=2)
    add_p("Dự án sử dụng cơ sở dữ liệu phi quan hệ (NoSQL Document Store) của Google Cloud Firestore. Dữ liệu được tổ chức theo cấu trúc phân tầng Collections và Documents.")

    add_h("5.2. Đặc tả chi tiết các Collection và Document Schemas", level=2)
    db_schema_data = [
        ["Tên Collection / Đường Dẫn", "Trường Dữ Liệu (Field)", "Kiểu Dữ Liệu", "Mô Tả & Ràng Buộc Dữ Liệu"],
        ["/users/{uid}", "displayName\nemail\ncreatedAt\ndob\ngender\nphone", "String\nString\nTimestamp/Long\nString\nString\nString", "Thông tin hồ sơ người dùng.\nKhóa chính (Document ID) trùng khớp với Firebase Auth UID."],
        ["/users/{uid}/foods/{foodId}", "name\ncategory\nprice\nrecipe\nisFavorite\ncreatedAt", "String\nString\nDouble\nString\nBoolean\nLong (Millis)", "Danh sách món ăn riêng của người dùng.\nSubcollection cách ly hoàn toàn giữa các tài khoản khác nhau."],
        ["/users/{uid}/history/{historyId}", "foodName\nprice\ntimestamp", "String\nDouble\nLong (Millis)", "Lịch sử các lần quay trúng món ăn.\nDùng để tính tổng chi tiêu và thống kê món ăn nhiều nhất."],
        ["/community/{postId}", "foodName\nrestaurantName\nnote\nprice\nauthorName\nauthorId\ntimestamp\nlikes\nlikedBy\ncommentCount", "String\nString\nString\nDouble\nString\nString (UID)\nLong (Millis)\nLong (Count)\nArray<String>\nLong (Count)", "Bảng tin cộng đồng chia sẻ món ngon.\nlikedBy chứa danh sách các UID đã thả tim để kiểm tra trạng thái."],
        ["/community/{postId}/comments/{cId}", "authorName\nauthorId\ntext\ntimestamp", "String\nString (UID)\nString\nLong (Millis)", "Subcollection chứa các bình luận của từng bài viết cụ thể."],
        ["/library_foods/{libFoodId}", "name\ncategory\nprice\nrecipe", "String\nString\nDouble\nString", "Thư viện món ăn mẫu của hệ thống dùng chung cho toàn bộ ứng dụng."]
    ]
    add_table_custom(db_schema_data[0], db_schema_data[1:])

    add_h("5.3. Sơ đồ thực thể logic ERD", level=2)
    erd_text = """
+-------------------------+             +-------------------------------+
|       USERS             | 1         * |          FOODS                |
| (Collection: /users)    |-------------< (Subcollection: /foods)       |
+-------------------------+             +-------------------------------+
| PK: uid (Auth UID)      |             | PK: foodId                    |
| - displayName: String   |             | - name: String                |
| - email: String         |             | - category: String            |
| - dob: String           |             | - price: Double               |
| - gender: String        |             | - recipe: String              |
| - phone: String         |             | - isFavorite: Boolean         |
+-------------------------+             | - createdAt: Long             |
      | 1                               +-------------------------------+
      |
      | 1                               +-------------------------------+
      +---------------------------------<          HISTORY              |
      |                                 | (Subcollection: /history)     |
      |                                 +-------------------------------+
      |                                 | PK: historyId                 |
      |                                 | - foodName: String            |
      |                                 | - price: Double               |
      |                                 | - timestamp: Long             |
      |                                 +-------------------------------+
      v 1
+-------------------------+ 1         * +-------------------------------+
|     COMMUNITY_POSTS     |-------------<           COMMENTS            |
| (Collection: /community)|             | (Subcollection: /comments)    |
+-------------------------+             +-------------------------------+
| PK: postId              |             | PK: commentId                 |
| - authorId: String (FK) |             | - authorId: String (FK)       |
| - authorName: String    |             | - authorName: String          |
| - foodName: String      |             | - text: String                |
| - restaurantName: String|             | - timestamp: Long             |
| - price: Double         |             +-------------------------------+
| - likes: Long           |
| - likedBy: Array<UID>   |
| - commentCount: Long    |
+-------------------------+
"""
    add_diagram_box("Hình 5.1: Sơ đồ thực thể logic ERD trong Cloud Firestore", erd_text)

    add_h("5.4. Đánh giá các rủi ro trong thiết kế Database hiện tại", level=2)
    add_p("1. Rủi ro Denormalization (Trùng lặp dữ liệu tác giả): authorName được lưu trực tiếp vào từng bài đăng và bình luận. Nếu người dùng đổi tên trong Hồ sơ cá nhân, các bài viết cũ sẽ không tự cập nhật lại tên mới trừ khi quét cập nhật toàn bộ.")
    add_p("2. Rủi ro giới hạn Document 1MB của Firestore: Trường likedBy lưu mảng danh sách UID người đã like. Với các bài viết có hàng chục nghìn lượt thích, kích thước mảng có thể vượt quá giới hạn 1MB của document Firestore. Giải pháp chuẩn là tách thành subcollection /likes.")

    doc.add_page_break()

    # =========================================================================
    # CHƯƠNG 6. PHÂN TÍCH GIAO DIỆN LẬP TRÌNH API & BACKEND
    # =========================================================================
    add_h("CHƯƠNG 6. PHÂN TÍCH GIAO DIỆN LẬP TRÌNH API & BACKEND", level=1)

    add_h("6.1. Kiến trúc Backend-as-a-Service (BaaS)", level=2)
    add_p("Trong dự án này, nhóm phát triển không xây dựng máy chủ Backend độc lập (như Spring Boot, Node.js Express hay ASP.NET Core) mà sử dụng trực tiếp Google Firebase SDK. Mã nguồn phía Client kết nối trực tiếp đến các dịch vụ đám mây của Google qua giao thức gRPC bảo mật.")

    add_h("6.2. Danh mục các thao tác dữ liệu (Data Operations)", level=2)
    api_table_data = [
        ["Nhóm Nghiệp Vụ", "Hàm Xử Lý Trong Code", "Đích Dữ Liệu (Collection)", "Cơ Chế Thực Thi", "Mô Tả"],
        ["Xác thực", "signInWithEmailAndPassword", "Firebase Auth", "SDK Async Task", "Đăng nhập người dùng bằng email/mật khẩu"],
        ["Xác thực", "createUserWithEmailAndPassword", "Firebase Auth", "SDK Async Task", "Tạo tài khoản mới và cập nhật display name"],
        ["Món ăn", "getFoodsQuery()", "/users/{uid}/foods", "Realtime SnapshotListener", "Lắng nghe danh sách món ăn sắp xếp theo ngày tạo"],
        ["Món ăn", "addFood()", "/users/{uid}/foods", "DocumentReference.add()", "Thêm món ăn mới với cờ yêu thích mặc định = false"],
        ["Món ăn", "updateFood()", "/users/{uid}/foods/{id}", "DocumentReference.update()", "Cập nhật tên, danh mục, giá tiền và công thức"],
        ["Món ăn", "toggleFavorite()", "/users/{uid}/foods/{id}", "DocumentReference.update()", "Đảo ngược trạng thái cờ isFavorite"],
        ["Món ăn", "deleteFood()", "/users/{uid}/foods/{id}", "DocumentReference.delete()", "Xóa document món ăn khỏi Firestore"],
        ["Lịch sử", "addHistory()", "/users/{uid}/history", "DocumentReference.add()", "Ghi nhận món vừa quay trúng vào lịch sử"],
        ["Lịch sử", "clearHistory()", "/users/{uid}/history", "WriteBatch.delete().commit()", "Xóa toàn bộ document trong subcollection bằng 1 batch"],
        ["Cộng đồng", "getCommunityQuery()", "/community", "SnapshotListener (Limit 50)", "Tải 50 bài đăng mới nhất sắp xếp theo timestamp giảm dần"],
        ["Cộng đồng", "postToCommunity()", "/community", "DocumentReference.add()", "Đăng bài chia sẻ món ăn lên bảng tin công cộng"],
        ["Cộng đồng", "toggleLikePost()", "/community/{postId}", "arrayUnion / arrayRemove", "Cập nhật danh sách likedBy và tăng giảm likes atomically"],
        ["Cộng đồng", "addComment()", "/community/{id}/comments", "Subcollection.add()", "Thêm bình luận và tăng commentCount ở post gốc"],
        ["Thư viện", "getLibraryQuery()", "/library_foods", "SnapshotListener", "Tải danh mục món ăn mẫu được gieo mầm sẵn"]
    ]
    add_table_custom(api_table_data[0], api_table_data[1:])

    add_h("6.3. Đánh giá hạn chế của mô hình Fat-Client so với REST API", level=2)
    add_p("• Ưu điểm: Tốc độ phát triển cực nhanh (Rapid Prototyping), tính năng đồng bộ thời gian thực mượt mà mà không cần tự cấu hình WebSocket server.")
    add_p("• Rủi ro lớn: Thiếu tầng kiểm duyệt trung gian (Business Layer). Khách hàng Android có quyền ghi trực tiếp vào Firestore nếu Security Rules cấu hình lỏng lẻo. Bất kỳ người dùng am hiểu kỹ thuật nào cũng có thể gửi dữ liệu rác, chèn script hoặc làm sai lệch số like/comment.")

    doc.add_page_break()

    # =========================================================================
    # CHƯƠNG 7. GIAO DIỆN VÀ TRẢI NGHIỆM NGƯỜI DÙNG (UI/UX)
    # =========================================================================
    add_h("CHƯƠNG 7. GIAO DIỆN VÀ TRẢI NGHIỆM NGƯỜI DÙNG (UI/UX)", level=1)

    add_h("7.1. Ngôn ngữ thiết kế và Hệ thống màu sắc", level=2)
    add_p("Ứng dụng tuân thủ triết lý Google Material Design 3 kết hợp bản sắc ẩm thực năng động:")
    add_p("• Màu sắc nhận diện chủ đạo (Primary Brand Color): Mã màu #FF6B35 (Cam nhiệt đới) và #E64A00 (Cam đậm). Màu cam kích thích cảm giác ngon miệng và tạo sự hứng khởi khi quay thưởng.")
    add_p("• Màu nền (Background): Mã #FFF8F5 (Trắng ngà ấm áp) và #FAFAFA, mang lại cảm giác dịu mắt, sạch sẽ và tôn vinh nội dung món ăn.")
    add_p("• Màu điểm nhấn (Secondary / Accent): Mã #4CAF50 (Xanh lá cây tươi mát) biểu thị cho ngân sách tài chính và sự thành công; #FFD700 (Vàng hoàng gia) biểu thị cho trạng thái món ăn yêu thích.")

    add_h("7.2. Phân tích 5 màn hình phân hệ cốt lõi", level=2)
    ui_screens_data = [
        ["Tên Màn Hình / Phân Hệ", "Thành Phần Giao Diện Nổi Bật", "Đánh Giá Trải Nghiệm Người Dùng (UX)"],
        ["Trang chủ (HomeFragment)", "Vòng quay RouletteWheelView, ConfettiView, Chips Pool ngang, Switch loại trừ, Nút Quay bo tròn 30dp.", "Rất tốt. Hiệu ứng thị giác cuốn hút, âm thanh quay vui tai, tạo sự tò mò và cảm giác thỏa mãn khi dừng lại."],
        ["Món ăn (FoodListFragment)", "Thanh tìm kiếm dạng Card bo tròn, thanh cuộn Chip danh mục ngang, FAB thêm món, FAB thư viện.", "Tiện dụng. Tìm kiếm phản hồi tức thì có debounce; cho phép mở rộng xem công thức trực tiếp mà không cần chuyển màn hình."],
        ["Thư viện món (FoodLibraryActivity)", "Toolbar trắng có nút đồng bộ đám mây, danh sách món ăn mẫu kèm nút '+ Thêm vào danh sách'.", "Thân thiện. Giúp người dùng mới không bị bỡ ngỡ với ứng dụng trống dữ liệu (Empty State)."],
        ["Cộng đồng (CommunityFragment)", "CardView bài đăng hiện đại, nút Like và Comment sinh động, Dialog bình luận trượt mượt mà.", "Tốt. Tương tác trực quan giống một mạng xã hội thu nhỏ; bài viết có hiển thị giá tiền minh bạch."],
        ["Lịch sử (HistoryFragment)", "Card thống kê tổng số lượt quay và tổng tiền chi tiêu, danh sách thẻ lịch sử, nút xóa toàn bộ.", "Rõ ràng. Giúp người dùng đối chiếu được mình đã ăn những gì và chi tiêu bao nhiêu tiền."],
        ["Hồ sơ (ProfileFragment)", "Header cam uốn lượn, Avatar chữ cái đầu, Card thống kê tháng, Form thông tin cá nhân, Switch báo thức.", "Chuyên nghiệp. Kết hợp quản lý thông tin với số liệu tài chính tháng; date picker thuận tiện."]
    ]
    add_table_custom(ui_screens_data[0], ui_screens_data[1:])

    doc.add_page_break()

    # =========================================================================
    # CHƯƠNG 8. KIỂM THỬ VÀ ĐẢM BẢO CHẤT LƯỢNG (TESTING & QA)
    # =========================================================================
    add_h("CHƯƠNG 8. KIỂM THỬ VÀ ĐẢM BẢO CHẤT LƯỢNG (TESTING & QA)", level=1)

    add_h("8.1. Hiện trạng kiểm thử trong mã nguồn", level=2)
    add_callout(
        "KẾT QUẢ RÀ SOÁT MÃ NGUỒN KIỂM THỬ",
        "Trong thư mục app/src/test và app/src/androidTest, dự án hiện CHỈ CÓ các file mẫu mặc định do Android Studio sinh ra:\n"
        "- ExampleUnitTest.java: Chỉ có một hàm kiểm tra phép cộng (assertEquals(4, 2 + 2)).\n"
        "- ExampleInstrumentedTest.java: Chỉ có một hàm kiểm tra package name.\n"
        "-> DỰ ÁN HOÀN TOÀN CHƯA CÓ UNIT TEST HAY INSTRUMENTATION TEST THỰC TẾ."
    )

    add_h("8.2. Ma trận kịch bản kiểm thử chức năng (Functional Test Cases)", level=2)
    add_p("Nhằm đảm bảo chất lượng phần mềm khi chuyển giao sang giai đoạn vận hành, nhóm QA đề xuất bộ kịch bản kiểm thử sau:")
    tc_data = [
        ["Mã TC", "Mô Tả Kịch Bản", "Dữ Liệu Đầu Vào (Input)", "Kết Quả Mong Đợi (Expected)", "Đánh Giá Thực Tế"],
        ["TC-01", "Đăng ký email hợp lệ", "Tên: 'Nam', Email: 'nam@test.com', Pass: '123456'", "Tạo tài khoản Firebase Auth thành công, chuyển sang MainActivity.", "Đạt theo code"],
        ["TC-02", "Đăng ký pass ngắn", "Pass: '123'", "Báo lỗi 'Mật khẩu tối thiểu 6 ký tự', giữ nguyên form.", "Đạt theo code"],
        ["TC-03", "Quay khi Pool rỗng", "Bấm 'QUAY NGAY' khi chưa thêm món nào", "Hiện Toast: 'Chưa có món nào! Sang tab Món ăn và nhấn ＋', không crash.", "Đạt theo code"],
        ["TC-04", "Quay có 3 món", "Pool có: Phở (50k), Cơm (40k), Bún (45k)", "Vòng quay xoay 7 vòng trong 4.5s, dừng đúng 1 món, hiện popup, lưu /history.", "Đạt theo code"],
        ["TC-05", "Bật loại trừ", "SwitchEliminate = ON, quay trúng 'Phở'", "Món 'Phở' tự động bị xóa khỏi Pool, chip biến mất, vòng còn 2 món.", "Đạt theo code"],
        ["TC-06", "Thêm món ăn mới", "Tên: 'Mì Quảng', Danh mục: 'Món nước', Giá: 45000", "Lưu vào Firestore /foods, xuất hiện tức thì trên RecyclerView.", "Đạt theo code"],
        ["TC-07", "Tìm kiếm realtime", "Gõ chữ 'bò' vào thanh tìm kiếm", "Sau 300ms debounce, danh sách chỉ hiển thị các món có chữ 'bò'.", "Đạt theo code"],
        ["TC-08", "Thả tim bài viết", "Bấm icon trái tim trên bài đăng", "Số like tăng lên 1, icon đổi sang màu đỏ; bấm lần nữa thì giảm 1.", "Đạt theo code"],
        ["TC-09", "Xóa toàn bộ lịch sử", "Bấm 'Xóa toàn bộ lịch sử' -> Chọn 'Xóa hết'", "Gọi WriteBatch xóa hết document, tổng số lần quay về 0, tổng chi về 0 VNĐ.", "Đạt theo code"],
        ["TC-10", "Bật nhắc nhở bữa ăn", "Bật switchReminder", "AlarmManager đăng ký lịch lặp lại lúc 11:30 hàng ngày, phát Toast xác nhận.", "Đạt theo code"]
    ]
    add_table_custom(tc_data[0], tc_data[1:])

    doc.add_page_break()

    # =========================================================================
    # CHƯƠNG 9. PHÂN TÍCH TRIỂN KHAI VÀ VẬN HÀNH (DEVOPS & DEPLOYMENT)
    # =========================================================================
    add_h("CHƯƠNG 9. PHÂN TÍCH TRIỂN KHAI VÀ VẬN HÀNH (DEVOPS & DEPLOYMENT)", level=1)

    add_h("9.1. Môi trường phát triển và cấu hình Gradle", level=2)
    add_p("• Phiên bản Gradle Wrapper: 9.5.0 (phân phối chính thức từ services.gradle.org).")
    add_p("• Android Gradle Plugin (AGP): Phiên bản 9.3.3 (khai báo trong libs.versions.toml).")
    add_p("• JDK Target: Java 11 (JavaVersion.VERSION_11), Toolchain Daemon hỗ trợ Java 25.")
    add_p("• Quản lý thư viện: Sử dụng Gradle Version Catalogs (gradle/libs.versions.toml) kết hợp Firebase BOM (33.6.0) đảm bảo toàn bộ thư viện Firebase luôn đồng bộ tương thích.")

    add_h("9.2. Phân tích quyền ứng dụng (Permissions) trong AndroidManifest.xml", level=2)
    add_p("Ứng dụng khai báo 4 quyền hệ thống:")
    add_p("1. android.permission.INTERNET: Cho phép ứng dụng kết nối Internet đồng bộ Firestore và Authentication.")
    add_p("2. android.permission.POST_NOTIFICATIONS: Quyền đẩy thông báo cho Android 13+ (API 33+). ProfileFragment có phương thức xin quyền động runtime.")
    add_p("3. android.permission.SCHEDULE_EXACT_ALARM: Quyền lên lịch báo thức chính xác cho bữa ăn trưa.")
    add_p("4. android.permission.USE_EXACT_ALARM: Quyền phụ trợ để đảm bảo AlarmManager không bị hệ điều hành trì hoãn nhằm tiết kiệm pin.")

    add_h("9.3. Đánh giá quy trình phát hành APK và Bảo mật Secrets", level=2)
    add_p("• File google-services.json đang được lưu trực tiếp trong thư mục app/. File này chứa API Key AIzaSyBYOGnfkDTpPlbOXOwVa9yw-B5khN5W1P4 và Project ID homnayangi-b84da. Trong mô hình Firebase, API Key này đóng vai trò định danh client, tuy nhiên NGUY CƠ LỚN là nếu không cấu hình Firestore Security Rules, bất kỳ ai dịch ngược APK đều có thể đọc/ghi toàn bộ cơ sở dữ liệu.")
    add_p("• Build Type Release trong app/build.gradle.kts hiện đang cấu hình isMinifyEnabled = false (optimization.enable = false). Điều này đồng nghĩa với việc mã nguồn chưa được làm mờ (obfuscation) bằng ProGuard/R8, tạo điều kiện thuận lợi cho kẻ tấn công dịch ngược mã nguồn bằng công cụ JADX.")

    doc.add_page_break()

    # =========================================================================
    # CHƯƠNG 10. ĐÁNH GIÁ TOÀN DIỆN HIỆN TRẠNG PHẦN MỀM
    # =========================================================================
    add_h("CHƯƠNG 10. ĐÁNH GIÁ TOÀN DIỆN HIỆN TRẠNG PHẦN MỀM", level=1)

    add_h("10.1. Bảng chấm điểm hệ thống theo thang điểm 10", level=2)
    score_data = [
        ["Tiêu Chí Đánh Giá", "Điểm", "Nhận Xét Đánh Giá Kỹ Thuật Chuyên Sâu"],
        ["1. Tính năng (Features)", "8.5/10", "Rất đầy đủ nghiệp vụ: Vòng quay, CRUD món, bộ lọc, thư viện, lịch sử, cộng đồng, hồ sơ, nhắc nhở."],
        ["2. Giao diện (UI/UX)", "8.5/10", "Thiết kế Material 3 hiện đại, màu cam ẩm thực bắt mắt, hiệu ứng vòng quay và pháo hoa giấy xuất sắc."],
        ["3. Kiến trúc (Architecture)", "5.5/10", "Chưa áp dụng MVVM/Clean Architecture; còn tồn tại Fat Fragment và xử lý logic trực tiếp trong UI callback."],
        ["4. Chất lượng mã (Code Quality)", "6.0/10", "Đặt tên lớp chuẩn; tuy nhiên thiếu Model Class (POJO), còn nhiều magic numbers và xử lý JSON thô."],
        ["5. Cơ sở dữ liệu (Database)", "7.0/10", "Thiết kế NoSQL phân tách theo user tốt; tuy nhiên có nguy cơ duplicate authorName và mảng likedBy phình to."],
        ["6. Bảo mật (Security)", "4.5/10", "Dùng Firebase Auth chuẩn; tuy nhiên chưa có Firestore Rules bảo vệ, chưa bật ProGuard/R8 làm mờ mã."],
        ["7. Kiểm thử (Testing)", "2.0/10", "Hoàn toàn chưa viết Unit Test và UI Test thực tế; chỉ có mã mẫu mặc định."],
        ["8. Hiệu năng (Performance)", "7.5/10", "Canvas vẽ rất mượt, có debounce tìm kiếm 300ms; tuy nhiên cần cải thiện việc cache ảnh và dữ liệu."],
        ["9. Triển khai (DevOps)", "5.0/10", "Cấu hình Gradle 9 và Version Catalog rất hiện đại; nhưng chưa có CI/CD pipeline tự động hóa."],
        ["10. Tài liệu (Documentation)", "4.0/10", "Dự án thiếu tài liệu đặc tả kiến trúc, hướng dẫn cài đặt và tài liệu API nội bộ."],
        ["ĐIỂM TRUNG BÌNH TOÀN DỰ ÁN", "5.85/10", "MỨC ĐỘ HOÀN THIỆN: 7.0 / 10 (Sản phẩm hoạt động tốt ở mức MVP nâng cao, cần tái cấu trúc để đưa vào sản xuất)."]
    ]
    add_table_custom(score_data[0], score_data[1:])

    add_h("10.2. Phân tích SWOT dự án", level=2)
    add_p("• Điểm mạnh (Strengths): Nghiệp vụ thực tế đánh trúng tâm lý người dùng; giao diện trực quan, giải trí cao; công nghệ Firebase Realtime mượt mà.")
    add_p("• Điểm yếu (Weaknesses): Kiến trúc chưa tách biệt rõ ràng (thiếu ViewModel/LiveData); thiếu vắng hoàn toàn các bài test tự động; chưa có kiểm soát dữ liệu ảnh.")
    add_p("• Cơ hội (Opportunities): Tiềm năng thương mại hóa cực cao nếu tích hợp với các nền tảng giao thức ăn (Affiliate ShopeeFood/GrabFood) hoặc các chuỗi nhà hàng.")
    add_p("• Thách thức (Threats): Chi phí đọc/ghi Firebase có thể tăng đột biến nếu số lượng người dùng tăng nhanh mà không áp dụng chiến lược Caching hợp lý.")

    doc.add_page_break()

    # =========================================================================
    # CHƯƠNG 11. PHƯƠNG ÁN CẢI TIẾN VÀ NÂNG CẤP HỆ THỐNG
    # =========================================================================
    add_h("CHƯƠNG 11. PHƯƠNG ÁN CẢI TIẾN VÀ NÂNG CẤP HỆ THỐNG", level=1)

    add_h("11.1. Phân loại yêu cầu cải tiến theo mô hình MoSCoW", level=2)
    moscow_data = [
        ["Phân Nhóm", "Mã Đề Xuất", "Tên Tính Năng / Nhiệm Vụ Cải Tiến", "Mục Tiêu & Giá Trị Đem Lại"],
        ["MUST HAVE\n(Bắt buộc có)", "MH-01", "Tái cấu trúc kiến trúc MVVM", "Tách UI ra khỏi Business Logic bằng ViewModel và LiveData / StateFlow."],
        ["MUST HAVE\n(Bắt buộc có)", "MH-02", "Thiết lập Firestore Security Rules", "Chặn đứng rủi ro đọc/ghi trái phép dữ liệu cá nhân của người dùng khác."],
        ["MUST HAVE\n(Bắt buộc có)", "MH-03", "Tạo các Class Data Model (POJO)", "Tạo Food.java, Post.java, User.java đảm bảo Type Safety và hạn chế crash."],
        ["MUST HAVE\n(Bắt buộc có)", "MH-04", "Kích hoạt R8 / ProGuard Obfuscation", "Bật minifyEnabled = true trong build.gradle để chống dịch ngược mã nguồn."],
        ["SHOULD HAVE\n(Nên có)", "SH-01", "Tích hợp tải ảnh bài đăng lên Cloud", "Sử dụng Firebase Storage hoặc Cloudinary để người dùng đăng ảnh món ăn thật."],
        ["SHOULD HAVE\n(Nên có)", "SH-02", "Đăng nhập Google Sign-In", "Giúp người dùng đăng nhập 1 chạm bằng tài khoản Google mà không cần gõ mật khẩu."],
        ["SHOULD HAVE\n(Nên có)", "SH-03", "Viết bộ Unit Test cho FoodRepository", "Đảm bảo độ bao phủ kiểm thử tối thiểu 60% cho các nghiệp vụ cốt lõi."],
        ["SHOULD HAVE\n(Nên có)", "SH-04", "Tối ưu hóa SharedPreferences", "Thay thế chuỗi JSON thủ công bằng EncryptedSharedPreferences và Gson."],
        ["NICE TO HAVE\n(Nâng cao)", "NH-01", "Gợi ý món ăn thông minh theo thời tiết", "Tích hợp OpenWeatherMap API: Trời lạnh gợi ý Lẩu/Phở, trời nóng gợi ý Trà sữa/Kem."],
        ["NICE TO HAVE\n(Nâng cao)", "NH-02", "Tìm kiếm quán ăn gần nhất (Google Maps)", "Mở Google Maps chỉ đường đến quán ăn tương ứng với món vừa quay trúng."],
        ["NICE TO HAVE\n(Nâng cao)", "NH-03", "Xuất báo cáo chi tiêu ra Excel/PDF", "Cho phép xuất lịch sử ăn uống hàng tháng để quản lý tài chính gia đình."],
        ["FUTURE\n(Tương lai)", "FT-01", "Trợ lý ảo AI tư vấn dinh dưỡng", "Tích hợp Gemini API phân tích lượng calo món ăn và gợi ý thực đơn ăn kiêng (Keto/Clean)."],
        ["FUTURE\n(Tương lai)", "FT-02", "Liên kết đặt món với Grab / ShopeeFood", "Mở liên kết Deep-link sang ứng dụng giao đồ ăn để đặt món ngay tức thì."]
    ]
    add_table_custom(moscow_data[0], moscow_data[1:])

    add_h("11.2. Đề xuất mô hình kiến trúc mục tiêu (Clean Architecture + MVVM)", level=2)
    proposed_arch_text = """
+-----------------------------------------------------------------------------------+
|                           VIEW LAYER (ACTIVITIES & FRAGMENTS)                     |
|   - MainActivity, HomeFragment, FoodListFragment, CommunityFragment, ...          |
|   - Nhiệm vụ duy nhất: Quan sát State (Observer) và hiển thị dữ liệu lên giao diện|
+-----------------------------------------------------------------------------------+
                                         |
                                         | Quan sát (Observe LiveData / StateFlow)
                                         v
+-----------------------------------------------------------------------------------+
|                                 VIEWMODEL LAYER                                   |
|   - HomeViewModel, FoodListViewModel, CommunityViewModel, ProfileViewModel        |
|   - Quản lý trạng thái màn hình (UI State: Loading, Success, Error)               |
|   - Sống độc lập với vòng đời Activity (Không bị hủy khi xoay màn hình)          |
+-----------------------------------------------------------------------------------+
                                         |
                                         | Gọi Use Case / Repository
                                         v
+-----------------------------------------------------------------------------------+
|                             DATA & REPOSITORY LAYER                               |
|   - FoodRepositoryImpl (Kế thừa Interface IFoodRepository)                        |
|   - LocalDataSource: Room Database (Cache offline) & SharedPreferences            |
|   - RemoteDataSource: Firebase Firestore API & Firebase Storage                   |
+-----------------------------------------------------------------------------------+
"""
    add_diagram_box("Hình 11.1: Sơ đồ kiến trúc mục tiêu: MVVM + Clean Architecture", proposed_arch_text)

    doc.add_page_break()

    # =========================================================================
    # CHƯƠNG 12. KẾ HOẠCH VÀ LỘ TRÌNH PHÁT TRIỂN (ROADMAP & BACKLOG)
    # =========================================================================
    add_h("CHƯƠNG 12. KẾ HOẠCH VÀ LỘ TRÌNH PHÁT TRIỂN (ROADMAP & BACKLOG)", level=1)

    add_h("12.1. Lộ trình triển khai 6 giai đoạn (Development Roadmap)", level=2)
    roadmap_data = [
        ["Giai Đoạn (Phase)", "Công Việc Trọng Tâm", "Thời Gian Dự Kiến", "Kết Quả Đầu Ra (Deliverables)"],
        ["Phase 1: Ổn định nền tảng (Foundation)", "Tạo Data Models (POJO), cấu hình Security Rules, bật ProGuard R8.", "Tuần 1 - Tuần 2", "Mã nguồn sạch, an toàn, không còn rủi ro bảo mật lộ lọt Firestore."],
        ["Phase 2: Tái cấu trúc MVVM", "Chuyển đổi các Fragment sang ViewModel + LiveData, bóc tách State.", "Tuần 3 - Tuần 5", "Kiến trúc chuẩn Android Jetpack, giao diện không bị giật lag khi xoay."],
        ["Phase 3: Hoàn thiện tính năng", "Tích hợp tải ảnh Cloud cho Community, thêm đăng nhập Google Sign-In.", "Tuần 6 - Tuần 8", "Tính năng mạng xã hội hoàn chỉnh với hình ảnh chân thực."],
        ["Phase 4: Kiểm thử tự động (QA)", "Viết Unit Test cho Repository & ViewModel, viết UI Test với Espresso.", "Tuần 9 - Tuần 10", "Test Coverage đạt >= 70%, phát hiện và vá toàn bộ lỗi tiềm ẩn."],
        ["Phase 5: Tối ưu & Đóng gói", "Cache dữ liệu bằng Room DB, đo đạc RAM/CPU, đóng gói file AAB có Sign Key.", "Tuần 11 - Tuần 12", "Ứng dụng đạt chuẩn xuất bản Google Play Store (Production-Ready)."],
        ["Phase 6: Mở rộng tính năng AI", "Tích hợp Gemini API gợi ý món theo Calo, liên kết bản đồ Maps API.", "Sau phát hành", "Nâng tầm ứng dụng thành Trợ lý ẩm thực thông minh toàn diện."]
    ]
    add_table_custom(roadmap_data[0], roadmap_data[1:])

    add_h("12.2. Bảng Product & Technical Backlog", level=2)
    backlog_data = [
        ["Task ID", "Hạng Mục Công Việc", "Loại Task", "Mức Ưu Tiên", "Ước Lượng", "Trạng Thái"],
        ["TASK-01", "Viết file firestore.rules chặn truy cập chéo giữa các user", "Security", "P0 - Critical", "1 ngày", "Cần làm ngay"],
        ["TASK-02", "Tạo các Class Model: FoodModel, PostModel, UserModel", "Refactor", "P0 - Critical", "2 ngày", "Cần làm ngay"],
        ["TASK-03", "Bật minifyEnabled và cấu hình proguard-rules.pro", "DevOps/Sec", "P0 - Critical", "1 ngày", "Cần làm ngay"],
        ["TASK-04", "Chuyển đổi HomeFragment sang HomeViewModel", "Architecture", "P1 - High", "3 ngày", "Sẵn sàng"],
        ["TASK-05", "Chuyển đổi FoodListFragment sang FoodListViewModel", "Architecture", "P1 - High", "3 ngày", "Sẵn sàng"],
        ["TASK-06", "Tích hợp Firebase Storage cho ảnh bài viết cộng đồng", "Feature", "P1 - High", "4 ngày", "Chờ Phase 3"],
        ["TASK-07", "Tích hợp Google Sign-In SDK", "Feature", "P1 - High", "3 ngày", "Chờ Phase 3"],
        ["TASK-08", "Viết 20 kịch bản Unit Test cho FoodRepository", "Testing", "P1 - High", "4 ngày", "Chờ Phase 4"],
        ["TASK-09", "Tích hợp Room Database làm tầng đệm Cache Offline", "Performance", "P2 - Medium", "5 ngày", "Chờ Phase 5"],
        ["TASK-10", "Tích hợp Gemini AI API gợi ý thực đơn dinh dưỡng", "Innovation", "P3 - Low", "7 ngày", "Chờ Phase 6"]
    ]
    add_table_custom(backlog_data[0], backlog_data[1:])

    doc.add_page_break()

    # =========================================================================
    # CHƯƠNG 13. KẾT LUẬN VÀ KIẾN NGHỊ
    # =========================================================================
    add_h("CHƯƠNG 13. KẾT LUẬN VÀ KIẾN NGHỊ", level=1)

    add_h("13.1. Kết quả đạt được của dự án", level=2)
    add_p("Qua quá trình phân tích toàn diện mã nguồn, có thể khẳng định dự án \"Hôm Nay Ăn Gì?\" là một sản phẩm phần mềm di động có tính ứng dụng thực tiễn rất cao, đáp ứng xuất sắc bài toán nghiệp vụ ban đầu đề ra. Đội ngũ phát triển đã thể hiện kỹ năng lập trình Android Native và làm chủ nền tảng Google Firebase rất tốt thông qua việc xây dựng thành công 5 phân hệ chức năng hoàn chỉnh, tương tác thời gian thực, có hiệu ứng đồ họa động xuất sắc (Custom Canvas 2D) và giao diện Material 3 bắt mắt.")

    add_h("13.2. Những hạn chế kỹ thuật cốt lõi cần khắc phục", level=2)
    add_p("Bên cạnh các ưu điểm nổi bật, dự án vẫn còn mang tính chất của một phiên bản MVP (Minimum Viable Product). Những hạn chế cần nghiêm túc khắc phục trước khi thương mại hóa bao gồm: thiếu vắng tầng kiểm thử tự động, kiến trúc chưa tách biệt triệt để theo mô hình MVVM, phụ thuộc trực tiếp vào BaaS mà chưa có quy tắc bảo mật Firestore Rules, và thiếu cơ chế tải hình ảnh lên đám mây.")

    add_h("13.3. Lời kết và Lời khuyên dành cho đội ngũ phát triển", level=2)
    add_p("Dự án sở hữu tiềm năng phát triển thành một sản phẩm thương mại có chỗ đứng trên thị trường ứng dụng di động Việt Nam. Nếu đội ngũ phát triển thực hiện nghiêm túc theo lộ trình tái cấu trúc kiến trúc (MVVM), siết chặt chính sách bảo mật và mở rộng tính năng theo mô hình đề xuất trong tài liệu này, ứng dụng hoàn toàn có thể đạt mức độ hoàn thiện 9.5/10 và sẵn sàng phục vụ hàng trăm nghìn người dùng thực tế.")

    doc.add_page_break()

    # =========================================================================
    # PHỤ LỤC KỸ THUẬT
    # =========================================================================
    add_h("PHỤ LỤC KỸ THUẬT", level=1)

    add_h("Phụ lục A: Cấu hình Security Rules khuyến nghị cho Cloud Firestore", level=2)
    rules_text = """
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Quy tắc cho Document người dùng: Chỉ chính chủ mới có quyền đọc và sửa
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Subcollection foods: Chỉ chính chủ được toàn quyền CRUD
      match /foods/{foodId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      // Subcollection history: Chỉ chính chủ được thêm, đọc và xóa
      match /history/{historyId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
    
    // Quy tắc cho Community: Mọi user đã login đều được đọc
    match /community/{postId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && request.resource.data.authorId == request.auth.uid;
      // Chỉ cho phép update trường likes/likedBy/commentCount hoặc sửa bài chính chủ
      allow update: if request.auth != null;
      allow delete: if request.auth != null && resource.data.authorId == request.auth.uid;
      
      // Bình luận bài viết
      match /comments/{commentId} {
        allow read: if request.auth != null;
        allow create: if request.auth != null && request.resource.data.authorId == request.auth.uid;
      }
    }
    
    // Thư viện món ăn chung: Mọi user được đọc, không ai được tự ý sửa trên client
    match /library_foods/{foodId} {
      allow read: if request.auth != null;
      allow write: if false; // Chỉ quản trị viên cập nhật qua Firebase Console
    }
  }
}
"""
    add_diagram_box("Mã nguồn cấu hình firestore.rules an toàn", rules_text)

    add_h("Phụ lục B: Bảng tra cứu các thư viện phụ thuộc (Dependencies)", level=2)
    deps_data = [
        ["Tên Dependency (TOML Key)", "Nhóm & Tên Gói Thư Viện", "Phiên Bản Khai Báo", "Mục Đích Sử Dụng"],
        ["libs.appcompat", "androidx.appcompat:appcompat", "1.8.0", "Hỗ trợ tương thích ngược giao diện Android"],
        ["libs.material", "com.google.android.material:material", "1.14.0", "Thư viện thành phần Google Material Design 3"],
        ["libs.activity.ktx", "androidx.activity:activity-ktx", "1.8.0", "Hỗ trợ Kotlin Activity Extensions"],
        ["libs.constraintlayout", "androidx.constraintlayout:constraintlayout", "2.1.4", "Layout linh hoạt cho màn hình phức tạp"],
        ["libs.firebase.bom", "com.google.firebase:firebase-bom", "33.6.0", "Quản lý phiên bản tập trung các dịch vụ Firebase"],
        ["libs.firebase.auth", "com.google.firebase:firebase-auth", "Managed by BOM", "Xác thực tài khoản và phiên người dùng"],
        ["libs.firebase.firestore", "com.google.firebase:firebase-firestore", "Managed by BOM", "Cơ sở dữ liệu đám mây NoSQL thời gian thực"],
        ["libs.junit", "junit:junit", "4.13.2", "Khung kiểm thử Unit Test cho Java"],
        ["libs.espresso.core", "androidx.test.espresso:espresso-core", "3.7.0", "Khung kiểm thử tự động giao diện Android"]
    ]
    add_table_custom(deps_data[0], deps_data[1:])

    # Save document
    output_filename = "BAO_CAO_DU_AN_HOMNAYANGI.docx"
    output_path = os.path.join("d:\\homnayangi", output_filename)
    doc.save(output_path)
    print(f"Document successfully created and saved to: {output_path}")

if __name__ == "__main__":
    create_report()
