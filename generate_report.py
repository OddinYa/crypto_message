#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Скрипт генерации отчета по приложению RyptoMessage
"""

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import inch, cm
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Image, Table, TableStyle, PageBreak
from reportlab.lib.enums import TA_CENTER, TA_LEFT, TA_JUSTIFY
from reportlab.pdfgen import canvas
from PIL import Image as PILImage
import os

def create_algorithm_diagram():
    """Создает схему алгоритма работы приложения"""
    
    # Создаем изображение с диаграммой
    width, height = 800, 1200
    img = PILImage.new('RGB', (width, height), 'white')
    
    from PIL import ImageDraw, ImageFont
    
    draw = ImageDraw.Draw(img)
    
    # Попытка загрузить шрифт с поддержкой кириллицы
    try:
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 16)
        title_font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 20)
        small_font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 12)
    except:
        font = ImageFont.load_default()
        title_font = font
        small_font = font
    
    # Цвета
    start_color = "#90EE90"  # светло-зеленый
    process_color = "#87CEEB"  # светло-голубой
    decision_color = "#FFD700"  # золотой
    data_color = "#DDA0DD"  # сливовый
    end_color = "#FFB6C1"  # светло-красный
    
    def draw_rounded_rect(x, y, w, h, color, text=""):
        draw.rounded_rectangle([x, y, x+w, y+h], radius=15, fill=color, outline="black")
        if text:
            bbox = draw.textbbox((0, 0), text, font=font)
            text_w = bbox[2] - bbox[0]
            text_h = bbox[3] - bbox[1]
            draw.text(((x + w - text_w) // 2, y + (h - text_h) // 2), text, fill="black", font=font)
    
    def draw_rect(x, y, w, h, color, text=""):
        draw.rectangle([x, y, x+w, y+h], fill=color, outline="black")
        if text:
            bbox = draw.textbbox((0, 0), text, font=font)
            text_w = bbox[2] - bbox[0]
            text_h = bbox[3] - bbox[1]
            draw.text(((x + w - text_w) // 2, y + (h - text_h) // 2), text, fill="black", font=font)
    
    def draw_diamond(x, y, w, h, color, text=""):
        points = [
            (x + w//2, y),
            (x + w, y + h//2),
            (x + w//2, y + h),
            (x, y + h//2)
        ]
        draw.polygon(points, fill=color, outline="black")
        if text:
            bbox = draw.textbbox((0, 0), text, font=small_font)
            text_w = bbox[2] - bbox[0]
            text_h = bbox[3] - bbox[1]
            draw.text(((x + w - text_w) // 2, y + (h - text_h) // 2), text, fill="black", font=small_font)
    
    def draw_line(x1, y1, x2, y2):
        draw.line([(x1, y1), (x2, y2)], fill="black", width=2)
        # Стрелка
        if y2 > y1:
            arrow_y = y2 - 5
            draw.polygon([(x2-5, arrow_y), (x2+5, arrow_y), (x2, y2)], fill="black")
        elif x2 > x1:
            arrow_x = x2 - 5
            draw.polygon([(arrow_x, y2-5), (arrow_x, y2+5), (x2, y2)], fill="black")
    
    # Заголовок
    draw.text((width//2 - 150, 20), "Алгоритм работы приложения", fill="black", font=title_font)
    draw.text((width//2 - 100, 45), "RyptoMessage", fill="black", font=title_font)
    
    y_offset = 80
    
    # Start
    draw_rounded_rect(width//2 - 100, y_offset, 200, 50, start_color, "Запуск приложения")
    y_offset += 80
    
    # Line
    draw_line(width//2, y_offset - 30, width//2, y_offset)
    
    # Проверка ключей
    draw_diamond(width//2 - 120, y_offset, 240, 60, decision_color, "Ключи существуют?")
    y_offset += 90
    
    # Line No
    draw_line(width//2 - 60, y_offset - 30, width//2 - 60, y_offset)
    draw.text((width//2 - 100, y_offset - 25), "Нет", fill="black", font=small_font)
    
    # Генерация ключей
    draw_rect(width//2 - 200, y_offset, 280, 50, process_color, "Генерация пары RSA ключей\n(2048 бит)")
    y_offset += 80
    
    # Line
    draw_line(width//2 - 60, y_offset - 30, width//2 - 60, y_offset + 20)
    draw_line(width//2 + 60, y_offset - 110, width//2 + 60, y_offset - 30)
    draw_line(width//2 + 60, y_offset - 30, width//2, y_offset - 30)
    draw.text((width//2 + 65, y_offset - 80), "Да", fill="black", font=small_font)
    
    # Сохранение в SharedPreferences
    draw_rect(width//2 - 200, y_offset + 20, 280, 50, data_color, "Сохранение ключей в\nSharedPreferences")
    y_offset += 100
    
    # Line
    draw_line(width//2, y_offset - 30, width//2, y_offset)
    
    # Главный экран
    draw_rounded_rect(width//2 - 100, y_offset, 200, 50, process_color, "Главный экран\n(3 вкладки)")
    y_offset += 80
    
    # Line
    draw_line(width//2, y_offset - 30, width//2, y_offset)
    
    # Вкладка 1: Мой QR
    draw_rect(50, y_offset, 200, 80, process_color, "Вкладка 1:\nМой QR Код\n- Отображение QR\n- Копирование ключа\n- Сохранение никнейма")
    
    # Вкладка 2: Зашифровать
    draw_rect(300, y_offset, 200, 80, process_color, "Вкладка 2:\nЗашифровать\n- Сканирование QR контакта\n- Выбор получателя\n- Шифрование сообщения")
    
    # Вкладка 3: Расшифровать
    draw_rect(550, y_offset, 200, 80, process_color, "Вкладка 3:\nРасшифровать\n- Вставка сообщения\n- Выбор отправителя\n- Расшифровка")
    
    y_offset += 120
    
    # Line
    draw_line(width//2, y_offset - 40, width//2, y_offset)
    
    # Детали шифрования
    draw_rect(width//2 - 250, y_offset, 500, 100, data_color, 
              "Алгоритм шифрования RSA:\n1. Получение публичного ключа получателя\n2. Шифрование сообщения (RSA/ECB/PKCS1Padding)\n3. Добавление публичного ключа отправителя\n4. Копирование в буфер обмена")
    
    y_offset += 130
    
    # Line
    draw_line(width//2, y_offset - 30, width//2, y_offset)
    
    # Детали расшифровки
    draw_rect(width//2 - 250, y_offset, 500, 100, data_color,
              "Алгоритм расшифровки RSA:\n1. Получение зашифрованного сообщения\n2. Извлечение приватного ключа\n3. Расшифровка сообщения\n4. Определение отправителя по ключу")
    
    y_offset += 130
    
    # Line
    draw_line(width//2, y_offset - 30, width//2, y_offset)
    
    # End
    draw_rounded_rect(width//2 - 100, y_offset, 200, 50, end_color, "Работа завершена")
    
    # Сохраняем изображение
    img_path = "/workspace/app_algorithm.png"
    img.save(img_path)
    return img_path


def create_encryption_flowchart():
    """Создает схему процесса шифрования"""
    
    width, height = 700, 600
    img = PILImage.new('RGB', (width, height), 'white')
    
    from PIL import ImageDraw, ImageFont
    
    draw = ImageDraw.Draw(img)
    
    try:
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 14)
        title_font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 18)
    except:
        font = ImageFont.load_default()
        title_font = font
    
    def draw_box(x, y, w, h, color, text=""):
        draw.rectangle([x, y, x+w, y+h], fill=color, outline="black", width=2)
        if text:
            lines = text.split('\n')
            total_height = len(lines) * 18
            start_y = y + (h - total_height) // 2
            for i, line in enumerate(lines):
                bbox = draw.textbbox((0, 0), line, font=font)
                text_w = bbox[2] - bbox[0]
                draw.text((x + (w - text_w) // 2, start_y + i * 18), line, fill="black", font=font)
    
    def draw_arrow(x1, y1, x2, y2):
        draw.line([(x1, y1), (x2, y2)], fill="black", width=2)
        draw.polygon([(x2-8, y2-5), (x2+8, y2-5), (x2, y2)], fill="black")
    
    # Заголовок
    draw.text((width//2 - 100, 15), "Процесс шифрования", fill="black", font=title_font)
    
    # Отправитель
    draw_box(50, 50, 150, 60, "#FFE4B5", "Отправитель\n(Вы)")
    
    # Сообщение
    draw_box(250, 60, 120, 40, "#98FB98", "Сообщение")
    
    # Публичный ключ получателя
    draw_box(420, 60, 120, 40, "#87CEFA", "Публичный ключ\nполучателя")
    
    # Стрелки к шифрованию
    draw_arrow(200, 80, 250, 80)
    draw_arrow(370, 80, 420, 80)
    
    # Процесс шифрования
    draw_box(280, 150, 180, 80, "#DDA0DD", "CryptoManager.\nencryptMessage()\nRSA/ECB/PKCS1Padding")
    
    # Стрелки
    draw_arrow(310, 100, 310, 150)
    draw_arrow(480, 100, 460, 150)
    
    # Зашифрованное сообщение
    draw_box(280, 270, 180, 60, "#FFD700", "Зашифрованное\nсообщение\n(Base64)")
    
    draw_arrow(370, 230, 370, 270)
    
    # Формирование финального сообщения
    draw_box(280, 360, 180, 60, "#F0E68C", "Формат:\npublicKey_sender |\nencrypted_message")
    
    draw_arrow(370, 330, 370, 360)
    
    # Буфер обмена
    draw_box(280, 450, 180, 60, "#90EE90", "Буфер обмена\n(Clipboard)")
    
    draw_arrow(370, 420, 370, 450)
    
    # Получатель
    draw_box(550, 450, 120, 60, "#FFE4B5", "Получатель\n(Контакт)")
    
    draw_arrow(460, 480, 550, 480)
    
    img_path = "/workspace/encryption_flow.png"
    img.save(img_path)
    return img_path


def create_decryption_flowchart():
    """Создает схему процесса расшифровки"""
    
    width, height = 700, 600
    img = PILImage.new('RGB', (width, height), 'white')
    
    from PIL import ImageDraw, ImageFont
    
    draw = ImageDraw.Draw(img)
    
    try:
        font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 14)
        title_font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", 18)
    except:
        font = ImageFont.load_default()
        title_font = font
    
    def draw_box(x, y, w, h, color, text=""):
        draw.rectangle([x, y, x+w, y+h], fill=color, outline="black", width=2)
        if text:
            lines = text.split('\n')
            total_height = len(lines) * 18
            start_y = y + (h - total_height) // 2
            for i, line in enumerate(lines):
                bbox = draw.textbbox((0, 0), line, font=font)
                text_w = bbox[2] - bbox[0]
                draw.text((x + (w - text_w) // 2, start_y + i * 18), line, fill="black", font=font)
    
    def draw_arrow(x1, y1, x2, y2):
        draw.line([(x1, y1), (x2, y2)], fill="black", width=2)
        draw.polygon([(x2-8, y2-5), (x2+8, y2-5), (x2, y2)], fill="black")
    
    # Заголовок
    draw.text((width//2 - 100, 15), "Процесс расшифровки", fill="black", font=title_font)
    
    # Входные данные
    draw_box(50, 50, 150, 60, "#FFD700", "Зашифрованное\nсообщение\nиз буфера")
    
    # Приватный ключ
    draw_box(250, 60, 120, 40, "#FFB6C1", "Приватный ключ\n(из SharedPreferences)")
    
    # Стрелки
    draw_arrow(200, 80, 250, 80)
    
    # Процесс расшифровки
    draw_box(420, 50, 180, 80, "#DDA0DD", "CryptoManager.\ndecryptMessage()\nRSA/ECB/PKCS1Padding")
    
    draw_arrow(370, 80, 420, 80)
    
    # Расшифрованное сообщение
    draw_box(420, 170, 180, 60, "#98FB98", "Расшифрованное\nсообщение")
    
    draw_arrow(510, 130, 510, 170)
    
    # Определение отправителя
    draw_box(420, 260, 180, 70, "#87CEFA", "Поиск отправителя\nпо публичному ключу\nв контактах")
    
    draw_arrow(510, 230, 510, 260)
    
    # Результат
    draw_box(420, 360, 180, 80, "#90EE90", "Отображение:\n- Текст сообщения\n- Имя отправителя\n(если есть в контактах)")
    
    draw_arrow(510, 330, 510, 360)
    
    # Контакты
    draw_box(50, 360, 150, 80, "#F0E68C", "Контакты\n(SharedPreferences)\n- nickname\n- publicKey")
    
    draw_arrow(200, 400, 420, 400)
    
    img_path = "/workspace/decryption_flow.png"
    img.save(img_path)
    return img_path


def generate_pdf_report():
    """Генерирует PDF отчет"""
    
    doc = SimpleDocTemplate("/workspace/report.pdf", pagesize=A4)
    styles = getSampleStyleSheet()
    story = []
    
    # Стили
    title_style = ParagraphStyle(
        'CustomTitle',
        parent=styles['Heading1'],
        fontSize=24,
        textColor=colors.darkblue,
        spaceAfter=30,
        alignment=TA_CENTER
    )
    
    heading_style = ParagraphStyle(
        'CustomHeading',
        parent=styles['Heading2'],
        fontSize=16,
        textColor=colors.darkblue,
        spaceAfter=12,
        spaceBefore=12
    )
    
    subheading_style = ParagraphStyle(
        'CustomSubHeading',
        parent=styles['Heading3'],
        fontSize=14,
        textColor=colors.darkgreen,
        spaceAfter=10,
        spaceBefore=10
    )
    
    body_style = ParagraphStyle(
        'CustomBody',
        parent=styles['Normal'],
        fontSize=11,
        textColor=colors.black,
        spaceAfter=6,
        alignment=TA_JUSTIFY
    )
    
    # Заголовок
    story.append(Paragraph("Отчет по приложению RyptoMessage", title_style))
    story.append(Spacer(1, 0.2*inch))
    
    # Описание приложения
    story.append(Paragraph("1. Описание приложения", heading_style))
    story.append(Paragraph("""
        RyptoMessage — это Android-приложение для безопасной передачи зашифрованных сообщений 
        с использованием асимметричного шифрования RSA. Приложение позволяет пользователям 
        обмениваться зашифрованными сообщениями без необходимости передачи секретных ключей.
    """, body_style))
    story.append(Spacer(1, 0.1*inch))
    
    # Таблица с основной информацией
    table_data = [
        ['Параметр', 'Значение'],
        ['Тип шифрования', 'RSA (асимметричное)'],
        ['Длина ключа', '2048 бит'],
        ['Режим шифрования', 'RSA/ECB/PKCS1Padding'],
        ['Язык разработки', 'Kotlin'],
        ['Платформа', 'Android'],
        ['Хранение ключей', 'SharedPreferences'],
    ]
    
    table = Table(table_data, colWidths=[3*inch, 3*inch])
    table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.grey),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
        ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
        ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
        ('FONTSIZE', (0, 0), (-1, 0), 12),
        ('BOTTOMPADDING', (0, 0), (-1, 0), 12),
        ('BACKGROUND', (0, 1), (-1, -1), colors.beige),
        ('GRID', (0, 0), (-1, -1), 1, colors.black),
    ]))
    story.append(table)
    story.append(Spacer(1, 0.3*inch))
    
    # Архитектура приложения
    story.append(Paragraph("2. Архитектура приложения", heading_style))
    story.append(Paragraph("""
        Приложение построено по принципу Clean Architecture с разделением на слои:
    """, body_style))
    
    architecture_data = [
        ['Слой', 'Компоненты', 'Назначение'],
        ['UI', 'MainActivity, Fragments', 'Пользовательский интерфейс'],
        ['Data', 'PreferencesManager, Contact', 'Хранение данных'],
        ['Crypto', 'CryptoManager', 'Шифрование/расшифровка'],
    ]
    
    arch_table = Table(architecture_data, colWidths=[1.5*inch, 2.5*inch, 2.5*inch])
    arch_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.darkblue),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
        ('FONTSIZE', (0, 0), (-1, 0), 11),
        ('BOTTOMPADDING', (0, 0), (-1, 0), 10),
        ('BACKGROUND', (0, 1), (-1, -1), colors.lightgrey),
        ('GRID', (0, 0), (-1, -1), 1, colors.black),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
    ]))
    story.append(arch_table)
    story.append(Spacer(1, 0.3*inch))
    
    # Компоненты приложения
    story.append(Paragraph("3. Основные компоненты", heading_style))
    
    story.append(Paragraph("3.1 MainActivity", subheading_style))
    story.append(Paragraph("""
        Главная активность приложения, содержащая навигацию между тремя фрагментами 
        через BottomNavigationView. Использует ViewBinding для работы с UI.
    """, body_style))
    story.append(Spacer(1, 0.1*inch))
    
    story.append(Paragraph("3.2 CryptoManager", subheading_style))
    story.append(Paragraph("""
        Объект Kotlin, реализующий криптографические операции:
    """, body_style))
    
    crypto_methods = [
        ['Метод', 'Назначение'],
        ['generateKeyPair()', 'Генерация пары ключей RSA 2048 бит'],
        ['encryptMessage()', 'Шифрование сообщения публичным ключом'],
        ['decryptMessage()', 'Расшифровка сообщения приватным ключом'],
    ]
    
    crypto_table = Table(crypto_methods, colWidths=[2.5*inch, 4*inch])
    crypto_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.darkgreen),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
        ('GRID', (0, 0), (-1, -1), 1, colors.black),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
    ]))
    story.append(crypto_table)
    story.append(Spacer(1, 0.1*inch))
    
    story.append(Paragraph("3.3 PreferencesManager", subheading_style))
    story.append(Paragraph("""
        Класс для работы с SharedPreferences. Управляет хранением:
        - Никнейма пользователя
        - Публичного и приватного ключей
        - Списка контактов
        - Seed-фразы
    """, body_style))
    story.append(Spacer(1, 0.1*inch))
    
    story.append(Paragraph("3.4 Фрагменты", subheading_style))
    story.append(Paragraph("""
        • QRFragment — отображение QR-кода с публичным ключом пользователя
        • EncodeFragment — шифрование сообщений для контактов
        • DecodeFragment — расшифровка полученных сообщений
    """, body_style))
    story.append(PageBreak())
    
    # Алгоритм работы
    story.append(Paragraph("4. Алгоритм работы приложения", heading_style))
    story.append(Spacer(1, 0.2*inch))
    
    # Вставляем созданные диаграммы
    algorithm_img = create_algorithm_diagram()
    story.append(Paragraph("4.1 Общая схема алгоритма", subheading_style))
    story.append(Image(algorithm_img, width=6*inch, height=9*inch))
    story.append(PageBreak())
    
    story.append(Paragraph("4.2 Процесс шифрования", subheading_style))
    encryption_img = create_encryption_flowchart()
    story.append(Image(encryption_img, width=6*inch, height=5.5*inch))
    story.append(Spacer(1, 0.2*inch))
    
    story.append(Paragraph("""
        Процесс шифрования включает следующие шаги:
        1. Пользователь вводит текст сообщения
        2. Выбирает получателя из списка контактов
        3. Приложение получает публичный ключ получателя
        4. CryptoManager шифрует сообщение используя RSA
        5. К зашифрованному сообщению добавляется публичный ключ отправителя
        6. Результат копируется в буфер обмена
    """, body_style))
    story.append(PageBreak())
    
    story.append(Paragraph("4.3 Процесс расшифровки", subheading_style))
    decryption_img = create_decryption_flowchart()
    story.append(Image(decryption_img, width=6*inch, height=5.5*inch))
    story.append(Spacer(1, 0.2*inch))
    
    story.append(Paragraph("""
        Процесс расшифровки включает следующие шаги:
        1. Пользователь вставляет зашифрованное сообщение из буфера
        2. Приложение извлекает приватный ключ из SharedPreferences
        3. CryptoManager расшифровывает сообщение
        4. Определяется отправитель по публичному ключу
        5. Расшифрованный текст отображается пользователю
    """, body_style))
    story.append(Spacer(1, 0.3*inch))
    
    # Безопасность
    story.append(Paragraph("5. Вопросы безопасности", heading_style))
    story.append(Paragraph("""
        • Ключи RSA длиной 2048 бит обеспечивают надежную защиту
        • Приватный ключ никогда не покидает устройство
        • Общий секретный ключ генерируется на лету и не хранится
        • Для хранения ключей используется SharedPreferences
        • Поддерживается управление контактами с их публичными ключами
    """, body_style))
    story.append(Spacer(1, 0.3*inch))
    
    # Структура проекта
    story.append(Paragraph("6. Структура проекта", heading_style))
    
    structure_data = [
        ['Пакет', 'Файлы', 'Назначение'],
        ['ui', 'QRFragment, EncodeFragment, DecodeFragment', 'Фрагменты интерфейса'],
        ['crypto', 'CryptoManager.kt', 'Криптографические операции'],
        ['data', 'Contact.kt, PreferencesManager.kt', 'Модели и хранение данных'],
        ['root', 'MainActivity.kt', 'Главная активность'],
    ]
    
    struct_table = Table(structure_data, colWidths=[1.5*inch, 2.5*inch, 2.5*inch])
    struct_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.darkblue),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
        ('GRID', (0, 0), (-1, -1), 1, colors.black),
        ('VALIGN', (0, 0), (-1, -1), 'MIDDLE'),
    ]))
    story.append(struct_table)
    story.append(Spacer(1, 0.3*inch))
    
    # Вывод
    story.append(Paragraph("7. Выводы", heading_style))
    story.append(Paragraph("""
        Приложение RyptoMessage демонстрирует реализацию безопасной системы обмена 
        сообщениями на платформе Android с использованием современных подходов к разработке:
        Kotlin, Jetpack Navigation, ViewBinding. Применение асимметричного шифрования RSA 
        обеспечивает конфиденциальность переписки без необходимости предварительного обмена 
        секретными ключами между пользователями.
    """, body_style))
    
    # Build PDF
    doc.build(story)
    print("PDF отчет успешно создан: /workspace/report.pdf")
    
    return "/workspace/report.pdf"


if __name__ == "__main__":
    generate_pdf_report()
