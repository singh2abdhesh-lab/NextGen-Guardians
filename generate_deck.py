import os
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.dml.color import RGBColor
from pptx.enum.shapes import MSO_SHAPE

# ==============================================================================
# DESIGN SYSTEM CONSTANTS (Enterprise SDS Saffron Theme - Executive Corporate)
# ==============================================================================
COLOR_PRIMARY = RGBColor(212, 118, 10)       # Saffron #D4760A
COLOR_PRIMARY_DARK = RGBColor(184, 99, 4)   # #B86304
COLOR_BG = RGBColor(255, 249, 240)           # Cream #FFF9F0
COLOR_CARD = RGBColor(255, 255, 255)         # White #FFFFFF
COLOR_CARD_SUBTLE = RGBColor(253, 244, 231)  # Warm Surface #FDF4E7
COLOR_CARD_ACCENT = RGBColor(255, 243, 224)  # Saffron Tint #FFF3E0
COLOR_BORDER = RGBColor(232, 213, 181)       # Subtle Border #E8D5B5
COLOR_BORDER_STRONG = RGBColor(212, 189, 149)# Saffron-Toned Border #D4BD95
COLOR_TEXT = RGBColor(45, 36, 24)            # Dark Brown #2D2418
COLOR_MUTED = RGBColor(107, 79, 59)          # Muted Brown #6B4F3B
COLOR_DIM = RGBColor(156, 125, 100)          # Dim Caption #9C7D64
COLOR_GREEN = RGBColor(46, 125, 50)          # Enterprise Green #2E7D32
COLOR_WARNING = RGBColor(161, 98, 7)         # Amber Warning #A16207
COLOR_CRITICAL = RGBColor(198, 40, 40)       # Critical Red #C62828

FONT_HEADING = "Segoe UI"
FONT_BODY = "Calibri"
FONT_MONO = "Consolas"

SLIDE_WIDTH = Inches(13.333)
SLIDE_HEIGHT = Inches(7.5)

FOOTER_TEXT = "Enterprise AI Guardian  |  Digithon '26  |  Submitted by Abdhesh Singh, Dept - Knox Part"

prs = Presentation()
prs.slide_width = SLIDE_WIDTH
prs.slide_height = SLIDE_HEIGHT
blank_layout = prs.slide_layouts[6]

def add_slide_background(slide):
    bg_shape = slide.shapes.add_shape(
        MSO_SHAPE.RECTANGLE, 0, 0, SLIDE_WIDTH, SLIDE_HEIGHT
    )
    bg_shape.fill.solid()
    bg_shape.fill.fore_color.rgb = COLOR_BG
    bg_shape.line.fill.background()
    return bg_shape

def add_header(slide, tag_text, title_text, subtitle_text="", kpi_chip=""):
    # Header container
    header_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.38), Inches(11.733), Inches(1.3))
    tf = header_box.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0

    p_tag = tf.paragraphs[0]
    p_tag.text = tag_text.upper()
    p_tag.font.name = FONT_HEADING
    p_tag.font.size = Pt(9.5)
    p_tag.font.bold = True
    p_tag.font.color.rgb = COLOR_PRIMARY
    p_tag.space_after = Pt(2)

    p_title = tf.add_paragraph()
    p_title.text = title_text
    p_title.font.name = FONT_HEADING
    p_title.font.size = Pt(22)
    p_title.font.bold = True
    p_title.font.color.rgb = COLOR_TEXT
    p_title.space_after = Pt(2)

    if subtitle_text:
        p_sub = tf.add_paragraph()
        p_sub.text = subtitle_text
        p_sub.font.name = FONT_BODY
        p_sub.font.size = Pt(11)
        p_sub.font.color.rgb = COLOR_MUTED

    # Top-right KPI Chip if provided
    if kpi_chip:
        chip = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(10.2), Inches(0.42), Inches(2.333), Inches(0.42))
        chip.fill.solid()
        chip.fill.fore_color.rgb = COLOR_CARD_ACCENT
        chip.line.color.rgb = COLOR_PRIMARY
        chip.line.width = Pt(1.0)
        c_tf = chip.text_frame
        c_tf.vertical_anchor = MSO_ANCHOR.MIDDLE
        c_p = c_tf.paragraphs[0]
        c_p.alignment = PP_ALIGN.CENTER
        c_p.text = kpi_chip.upper()
        c_p.font.name = FONT_HEADING
        c_p.font.size = Pt(9)
        c_p.font.bold = True
        c_p.font.color.rgb = COLOR_PRIMARY

def add_footer(slide, slide_num, total_slides=21, pitch_cue=""):
    # Separator Line
    line = slide.shapes.add_shape(
        MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(6.88), Inches(11.733), Inches(0.015)
    )
    line.fill.solid()
    line.fill.fore_color.rgb = COLOR_BORDER
    line.line.fill.background()

    # Footer Left
    box_left = slide.shapes.add_textbox(Inches(0.8), Inches(6.94), Inches(8.5), Inches(0.38))
    tf_l = box_left.text_frame
    tf_l.word_wrap = True
    tf_l.margin_left = tf_l.margin_top = tf_l.margin_right = tf_l.margin_bottom = 0
    p_l = tf_l.paragraphs[0]
    p_l.text = FOOTER_TEXT
    p_l.font.name = FONT_BODY
    p_l.font.size = Pt(8.5)
    p_l.font.color.rgb = COLOR_DIM

    if pitch_cue:
        p_cue = tf_l.add_paragraph()
        p_cue.text = f"PITCH CUE (10-Min Pacing): {pitch_cue}"
        p_cue.font.name = FONT_BODY
        p_cue.font.size = Pt(7.5)
        p_cue.font.italic = True
        p_cue.font.color.rgb = COLOR_PRIMARY

    # Footer Right (Slide Number)
    box_right = slide.shapes.add_textbox(Inches(10.5), Inches(6.94), Inches(2.033), Inches(0.38))
    tf_r = box_right.text_frame
    tf_r.word_wrap = True
    tf_r.margin_left = tf_r.margin_top = tf_r.margin_right = tf_r.margin_bottom = 0
    p_r = tf_r.paragraphs[0]
    p_r.text = f"SLIDE {slide_num:02d} / {total_slides:02d}"
    p_r.alignment = PP_ALIGN.RIGHT
    p_r.font.name = FONT_HEADING
    p_r.font.size = Pt(8.5)
    p_r.font.bold = True
    p_r.font.color.rgb = COLOR_PRIMARY

def create_card(slide, left, top, width, height, bg_color=COLOR_CARD, border_color=COLOR_BORDER):
    card = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
    card.fill.solid()
    card.fill.fore_color.rgb = bg_color
    if border_color:
        card.line.color.rgb = border_color
        card.line.width = Pt(1.2)
    else:
        card.line.fill.background()
    return card

def create_screenshot_placeholder(slide, left, top, width, height, module_title, instruction_text):
    card = create_card(slide, left, top, width, height, bg_color=COLOR_CARD_ACCENT, border_color=COLOR_BORDER_STRONG)
    
    tb = slide.shapes.add_textbox(left + Inches(0.2), top + Inches(0.2), width - Inches(0.4), height - Inches(0.4))
    tf = tb.text_frame
    tf.word_wrap = True
    tf.vertical_anchor = MSO_ANCHOR.MIDDLE
    tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0

    p_badge = tf.paragraphs[0]
    p_badge.alignment = PP_ALIGN.CENTER
    p_badge.text = "[ APPLICATION SCREENSHOT REQUIRED ]"
    p_badge.font.name = FONT_HEADING
    p_badge.font.size = Pt(11)
    p_badge.font.bold = True
    p_badge.font.color.rgb = COLOR_PRIMARY
    p_badge.space_after = Pt(6)

    p_mod = tf.add_paragraph()
    p_mod.alignment = PP_ALIGN.CENTER
    p_mod.text = module_title
    p_mod.font.name = FONT_HEADING
    p_mod.font.size = Pt(13)
    p_mod.font.bold = True
    p_mod.font.color.rgb = COLOR_TEXT
    p_mod.space_after = Pt(8)

    p_desc = tf.add_paragraph()
    p_desc.alignment = PP_ALIGN.CENTER
    p_desc.text = instruction_text
    p_desc.font.name = FONT_BODY
    p_desc.font.size = Pt(9.5)
    p_desc.font.color.rgb = COLOR_MUTED

    p_note = tf.add_paragraph()
    p_note.alignment = PP_ALIGN.CENTER
    p_note.text = "Drop / Paste verified application screenshot here (16:9 ratio recommended)"
    p_note.font.name = FONT_BODY
    p_note.font.size = Pt(8.5)
    p_note.font.italic = True
    p_note.font.color.rgb = COLOR_DIM
    p_note.space_before = Pt(8)

# ==============================================================================
# SLIDE 1: TITLE PAGE (COVER) - EXECUTIVE PITCH
# ==============================================================================
slide1 = prs.slides.add_slide(blank_layout)
add_slide_background(slide1)

cover_card = create_card(slide1, Inches(0.8), Inches(0.7), Inches(11.733), Inches(6.0), bg_color=COLOR_CARD, border_color=COLOR_BORDER)

top_bar = slide1.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(0.7), Inches(11.733), Inches(0.12))
top_bar.fill.solid()
top_bar.fill.fore_color.rgb = COLOR_PRIMARY
top_bar.line.fill.background()

# Shield Icon badge
shield_box = slide1.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(1.4), Inches(1.3), Inches(0.85), Inches(0.85))
shield_box.fill.solid()
shield_box.fill.fore_color.rgb = COLOR_PRIMARY
shield_box.line.fill.background()
s_tf = shield_box.text_frame
s_tf.vertical_anchor = MSO_ANCHOR.MIDDLE
s_p = s_tf.paragraphs[0]
s_p.alignment = PP_ALIGN.CENTER
s_p.text = "🛡️"
s_p.font.size = Pt(28)

# Title & Subtitle block
tb_title = slide1.shapes.add_textbox(Inches(2.5), Inches(1.2), Inches(9.5), Inches(1.6))
tf_t = tb_title.text_frame
tf_t.word_wrap = True
tf_t.margin_left = tf_t.margin_top = tf_t.margin_right = tf_t.margin_bottom = 0

p1 = tf_t.paragraphs[0]
p1.text = "Enterprise AI Guardian"
p1.font.name = FONT_HEADING
p1.font.size = Pt(36)
p1.font.bold = True
p1.font.color.rgb = COLOR_TEXT

p2 = tf_t.add_paragraph()
p2.text = "AI Governance & Security Control Tower"
p2.font.name = FONT_HEADING
p2.font.size = Pt(18)
p2.font.bold = True
p2.font.color.rgb = COLOR_PRIMARY
p2.space_before = Pt(4)

p3 = tf_t.add_paragraph()
p3.text = "Zero-Bypass Inline Threat Mitigation • Real-Time Data Sanitization • Multi-Model Compliance Ledger"
p3.font.name = FONT_BODY
p3.font.size = Pt(11)
p3.font.color.rgb = COLOR_MUTED
p3.space_before = Pt(4)

div_line = slide1.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(1.4), Inches(3.2), Inches(10.533), Inches(0.015))
div_line.fill.solid()
div_line.fill.fore_color.rgb = COLOR_BORDER
div_line.line.fill.background()

# Meta details card left (Track & Event)
meta_card_1 = create_card(slide1, Inches(1.4), Inches(3.5), Inches(5.1), Inches(1.9), bg_color=COLOR_CARD_SUBTLE, border_color=COLOR_BORDER)
tb_m1 = slide1.shapes.add_textbox(Inches(1.65), Inches(3.7), Inches(4.6), Inches(1.5))
tf_m1 = tb_m1.text_frame
tf_m1.word_wrap = True
tf_m1.margin_left = tf_m1.margin_top = tf_m1.margin_right = tf_m1.margin_bottom = 0

p_m1_tag = tf_m1.paragraphs[0]
p_m1_tag.text = "INNOVATION TRACK & CATEGORY"
p_m1_tag.font.name = FONT_HEADING
p_m1_tag.font.size = Pt(9.5)
p_m1_tag.font.bold = True
p_m1_tag.font.color.rgb = COLOR_PRIMARY
p_m1_tag.space_after = Pt(4)

p_m1_body = tf_m1.add_paragraph()
p_m1_body.text = "Digithon '26  |  AI / AX\nAI Governance & Security Track"
p_m1_body.font.name = FONT_HEADING
p_m1_body.font.size = Pt(14)
p_m1_body.font.bold = True
p_m1_body.font.color.rgb = COLOR_TEXT

# Meta details card right (Author / Dept)
meta_card_2 = create_card(slide1, Inches(6.833), Inches(3.5), Inches(5.1), Inches(1.9), bg_color=COLOR_CARD_SUBTLE, border_color=COLOR_BORDER)
tb_m2 = slide1.shapes.add_textbox(Inches(7.083), Inches(3.7), Inches(4.6), Inches(1.5))
tf_m2 = tb_m2.text_frame
tf_m2.word_wrap = True
tf_m2.margin_left = tf_m2.margin_top = tf_m2.margin_right = tf_m2.margin_bottom = 0

p_m2_tag = tf_m2.paragraphs[0]
p_m2_tag.text = "SUBMITTED BY & DEPARTMENT"
p_m2_tag.font.name = FONT_HEADING
p_m2_tag.font.size = Pt(9.5)
p_m2_tag.font.bold = True
p_m2_tag.font.color.rgb = COLOR_PRIMARY
p_m2_tag.space_after = Pt(4)

p_m2_body = tf_m2.add_paragraph()
p_m2_body.text = "Abdhesh Singh\nDept - Knox Part"
p_m2_body.font.name = FONT_HEADING
p_m2_body.font.size = Pt(14)
p_m2_body.font.bold = True
p_m2_body.font.color.rgb = COLOR_TEXT

b_badge = slide1.shapes.add_textbox(Inches(1.4), Inches(5.65), Inches(10.533), Inches(0.4))
tf_bb = b_badge.text_frame
tf_bb.word_wrap = True
p_bb = tf_bb.paragraphs[0]
p_bb.text = "ENTERPRISE REAL-TIME INLINE AI GATEWAY & AUDIT OBSERVABILITY PLATFORM • 10-MINUTE JURY PITCH"
p_bb.font.name = FONT_HEADING
p_bb.font.size = Pt(9.5)
p_bb.font.bold = True
p_bb.font.color.rgb = COLOR_DIM
p_bb.alignment = PP_ALIGN.CENTER

add_footer(slide1, 1, pitch_cue="Introduce problem statement in 30 seconds: Generative AI is inevitable, but unmonitored adoption creates multi-million dollar data breach risks.")

# ==============================================================================
# SLIDE 2: THE BUSINESS PROBLEM
# ==============================================================================
slide2 = prs.slides.add_slide(blank_layout)
add_slide_background(slide2)
add_header(slide2, "The Enterprise AI Dilemma", "The AI Security Challenge: Innovation vs Exposure", "Uncontrolled Generative AI adoption creates irreversible intellectual property leakage and compliance liabilities.", kpi_chip="40%+ SHADOW AI USAGE")

c_left = create_card(slide2, Inches(0.8), Inches(1.85), Inches(5.7), Inches(4.8))
tb_l = slide2.shapes.add_textbox(Inches(1.1), Inches(2.1), Inches(5.1), Inches(4.3))
tf_l = tb_l.text_frame
tf_l.word_wrap = True
tf_l.margin_left = tf_l.margin_top = tf_l.margin_right = tf_l.margin_bottom = 0

p = tf_l.paragraphs[0]
p.text = "THE PRODUCTIVITY VS RISK PARADOX"
p.font.name = FONT_HEADING
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = COLOR_PRIMARY
p.space_after = Pt(8)

p = tf_l.add_paragraph()
p.text = "Employees are aggressively leveraging commercial AI assistants to draft code, summarize legal briefs, and parse telemetry. Without centralized inline guardrails, sensitive corporate IP routinely leaves enterprise boundaries into public cloud models."
p.font.name = FONT_BODY
p.font.size = Pt(12)
p.font.color.rgb = COLOR_TEXT
p.space_after = Pt(16)

callout = create_card(slide2, Inches(1.1), Inches(3.7), Inches(5.1), Inches(1.7), bg_color=COLOR_CARD_ACCENT, border_color=COLOR_PRIMARY)
tb_call = slide2.shapes.add_textbox(Inches(1.25), Inches(3.85), Inches(4.8), Inches(1.4))
tf_call = tb_call.text_frame
tf_call.word_wrap = True
tf_call.margin_left = tf_call.margin_top = tf_call.margin_right = tf_call.margin_bottom = 0
p = tf_call.paragraphs[0]
p.text = "CORE STRATEGIC IMPERATIVE:"
p.font.name = FONT_HEADING
p.font.size = Pt(10)
p.font.bold = True
p.font.color.rgb = COLOR_PRIMARY
p.space_after = Pt(4)
p = tf_call.add_paragraph()
p.text = "“Organizations cannot ban AI without stalling innovation. They must enforce visibility, sanitization, and policy before requests exit the perimeter.”"
p.font.name = FONT_HEADING
p.font.size = Pt(12)
p.font.bold = True
p.font.color.rgb = COLOR_TEXT

c_right = create_card(slide2, Inches(6.833), Inches(1.85), Inches(5.7), Inches(4.8))
tb_r = slide2.shapes.add_textbox(Inches(7.133), Inches(2.1), Inches(5.1), Inches(4.3))
tf_r = tb_r.text_frame
tf_r.word_wrap = True
tf_r.margin_left = tf_r.margin_top = tf_r.margin_right = tf_r.margin_bottom = 0

p = tf_r.paragraphs[0]
p.text = "5 LETHAL EXFILTRATION VECTORS"
p.font.name = FONT_HEADING
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = COLOR_PRIMARY
p.space_after = Pt(10)

threats = [
    ("Accidental Credential Leaks", "Hardcoded API keys, JWT tokens, AWS credentials, and database passwords pasted into prompts."),
    ("Customer PII & Sensitive Records", "Social Security Numbers, credit cards, customer emails, and patient records pasted for summarization."),
    ("Proprietary Source Code Exposure", "Core algorithmic logic, cryptographic secrets, and architectural blueprints submitted to cloud models."),
    ("Confidential Strategic Documents", "Unredacted financial earnings, M&A strategy drafts, and NDA-protected vendor contracts."),
    ("Adversarial Prompt Injections", "Jailbreak payloads, prompt leakage exploits, and system prompt bypass commands.")
]

for title, desc in threats:
    p_t = tf_r.add_paragraph()
    p_t.text = f"• {title}"
    p_t.font.name = FONT_HEADING
    p_t.font.size = Pt(11)
    p_t.font.bold = True
    p_t.font.color.rgb = COLOR_CRITICAL
    p_t.space_before = Pt(4)
    
    p_d = tf_r.add_paragraph()
    p_d.text = f"   {desc}"
    p_d.font.name = FONT_BODY
    p_d.font.size = Pt(9.5)
    p_d.font.color.rgb = COLOR_MUTED
    p_d.space_after = Pt(3)

add_footer(slide2, 2, pitch_cue="Emphasize: Blanket bans fail because employees bypass controls; inline intelligent governance is the only viable path.")

# ==============================================================================
# SLIDE 3: OUR SOLUTION
# ==============================================================================
slide3 = prs.slides.add_slide(blank_layout)
add_slide_background(slide3)
add_header(slide3, "The Architectural Answer", "Enterprise AI Guardian: The Inline Control Tower", "A zero-bypass security plane governing every prompt and completion in real time.", kpi_chip="< 45ms LATENCY OVERHEAD")

pipeline_nodes = [
    ("Employee", "Ingress Prompt", COLOR_MUTED),
    ("AI Gateway", "Proxy & Route", COLOR_PRIMARY),
    ("Security Scanner", "PII & Injection", COLOR_PRIMARY),
    ("Risk Engine", "Score (0-100)", COLOR_PRIMARY),
    ("Decision Engine", "Allow/Review/Block", COLOR_PRIMARY),
    ("AI Model", "Frontier / Mock", COLOR_GREEN),
    ("Quality Checker", "Hallucination/Safety", COLOR_PRIMARY),
    ("Audit Dashboard", "Kafka & H2 Ledger", COLOR_MUTED)
]

node_width = Inches(1.3)
node_height = Inches(1.4)
spacing = Inches(0.18)
start_left = Inches(0.8)
top_pos = Inches(1.9)

for idx, (title, sub, color) in enumerate(pipeline_nodes):
    cur_left = start_left + idx * (node_width + spacing)
    card = create_card(slide3, cur_left, top_pos, node_width, node_height, bg_color=COLOR_CARD, border_color=COLOR_BORDER)
    
    tb = slide3.shapes.add_textbox(cur_left + Inches(0.08), top_pos + Inches(0.12), node_width - Inches(0.16), node_height - Inches(0.24))
    tf = tb.text_frame
    tf.word_wrap = True
    tf.vertical_anchor = MSO_ANCHOR.MIDDLE
    tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0

    p_step = tf.paragraphs[0]
    p_step.alignment = PP_ALIGN.CENTER
    p_step.text = f"0{idx+1}"
    p_step.font.name = FONT_MONO
    p_step.font.size = Pt(9)
    p_step.font.bold = True
    p_step.font.color.rgb = color
    p_step.space_after = Pt(3)

    p_t = tf.add_paragraph()
    p_t.alignment = PP_ALIGN.CENTER
    p_t.text = title
    p_t.font.name = FONT_HEADING
    p_t.font.size = Pt(10.5)
    p_t.font.bold = True
    p_t.font.color.rgb = COLOR_TEXT
    p_t.space_after = Pt(3)

    p_s = tf.add_paragraph()
    p_s.alignment = PP_ALIGN.CENTER
    p_s.text = sub
    p_s.font.name = FONT_BODY
    p_s.font.size = Pt(8.5)
    p_s.font.color.rgb = COLOR_MUTED

    if idx < len(pipeline_nodes) - 1:
        arrow_box = slide3.shapes.add_textbox(cur_left + node_width, top_pos + Inches(0.45), spacing, Inches(0.4))
        a_tf = arrow_box.text_frame
        a_tf.margin_left = a_tf.margin_top = a_tf.margin_right = a_tf.margin_bottom = 0
        a_p = a_tf.paragraphs[0]
        a_p.alignment = PP_ALIGN.CENTER
        a_p.text = "→"
        a_p.font.size = Pt(13)
        a_p.font.bold = True
        a_p.font.color.rgb = COLOR_PRIMARY

pillars = [
    ("Zero-Bypass Architecture", "Acts as an impenetrable reverse proxy between corporate applications and upstream LLMs. Direct external model calls are blocked at DNS/gateway level.", "🛡️"),
    ("Automated Threat Neutralization", "Replaces sensitive identifiers (PII, API keys) on-the-fly via auto-redaction while terminating active jailbreaks and injection attempts instantaneously.", "⚡"),
    ("Immutable Enterprise Ledger", "Every prompt, decision, token consumption metric, and hallucination check is permanently captured in a streaming Kafka & H2/PostgreSQL compliance ledger.", "📋")
]

p_width = Inches(3.68)
p_gap = Inches(0.34)
for i, (p_title, p_desc, icon) in enumerate(pillars):
    p_left = Inches(0.8) + i * (p_width + p_gap)
    c = create_card(slide3, p_left, Inches(3.65), p_width, Inches(3.0), bg_color=COLOR_CARD, border_color=COLOR_BORDER)
    
    tb = slide3.shapes.add_textbox(p_left + Inches(0.3), Inches(3.9), p_width - Inches(0.6), Inches(2.5))
    tf = tb.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0

    p_icon = tf.paragraphs[0]
    p_icon.text = f"{icon}  {p_title}"
    p_icon.font.name = FONT_HEADING
    p_icon.font.size = Pt(13)
    p_icon.font.bold = True
    p_icon.font.color.rgb = COLOR_TEXT
    p_icon.space_after = Pt(10)

    p_d = tf.add_paragraph()
    p_d.text = p_desc
    p_d.font.name = FONT_BODY
    p_d.font.size = Pt(11)
    p_d.font.color.rgb = COLOR_MUTED
    p_d.space_after = Pt(10)

add_footer(slide3, 3, pitch_cue="Highlight: Unlike firewalls that only block, Guardian sanitizes and permits work to continue seamlessly.")

# ==============================================================================
# SLIDE 4: HIGH LEVEL ARCHITECTURE
# ==============================================================================
slide4 = prs.slides.add_slide(blank_layout)
add_slide_background(slide4)
add_header(slide4, "System Design & Resilience", "High-Level Architecture: Hexagonal Microservices", "Spring Boot 3.3 reactive microservices decoupled for high-concurrency, zero-lock throughput.", kpi_chip="ZERO EXTERNAL INFRA NEEDED")

arch_components = [
    ("AI Gateway (Port 8080)", "WebClient non-blocking reactive pipeline orchestrating 9-stage trace with circuit-breaker protection.", "INGRESS CONTROLLER"),
    ("Prompt Security Scanner (Port 8081)", "Multi-threaded pattern, regex, and heuristic detector scanning PII, credentials, keys, and jailbreaks.", "THREAT DETECTOR"),
    ("Risk Scoring Engine (Port 8082)", "Weighted multi-factor matrix computing composite risk score (0-100) and enforcement decisions.", "DECISION MATRIX"),
    ("LLM Connector & Mock Layer", "Unified OpenAI-compatible adapter supporting OpenAI, Claude, Gemini, and zero-cost mock completions.", "MODEL ROUTER"),
    ("Response Quality Checker (Port 8084)", "Post-generation verification validating response safety, factual alignment, and hallucination bounds.", "OUTPUT VALIDATOR"),
    ("Audit Service (Port 8083)", "Distributed Kafka event streaming with embedded H2 / PostgreSQL persistent compliance storage.", "AUDIT STREAM"),
    ("Governance Dashboard (Port 3000)", "Modern React 18 single-page application providing live telemetry, trace replay, and approval queues.", "CONTROL TOWER UI")
]

grid_w = Inches(3.68)
grid_h = Inches(2.2)
grid_gap_x = Inches(0.34)
grid_gap_y = Inches(0.25)

for idx, (title, desc, badge) in enumerate(arch_components):
    col = idx % 3
    row = idx // 3
    if idx == 6:
        c_left = Inches(0.8) + grid_w + grid_gap_x
        c_top = Inches(1.85) + 2 * (grid_h + grid_gap_y) - Inches(0.1)
        grid_w_final = grid_w
    else:
        c_left = Inches(0.8) + col * (grid_w + grid_gap_x)
        c_top = Inches(1.85) + row * (grid_h + grid_gap_y)
        grid_w_final = grid_w
        
    if row < 2:
        card = create_card(slide4, c_left, c_top, grid_w_final, grid_h, bg_color=COLOR_CARD, border_color=COLOR_BORDER)
        tb = slide4.shapes.add_textbox(c_left + Inches(0.25), c_top + Inches(0.2), grid_w_final - Inches(0.5), grid_h - Inches(0.4))
        tf = tb.text_frame
        tf.word_wrap = True
        tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0

        p_b = tf.paragraphs[0]
        p_b.text = badge
        p_b.font.name = FONT_HEADING
        p_b.font.size = Pt(8.5)
        p_b.font.bold = True
        p_b.font.color.rgb = COLOR_PRIMARY
        p_b.space_after = Pt(4)

        p_t = tf.add_paragraph()
        p_t.text = title
        p_t.font.name = FONT_HEADING
        p_t.font.size = Pt(11.5)
        p_t.font.bold = True
        p_t.font.color.rgb = COLOR_TEXT
        p_t.space_after = Pt(4)

        p_d = tf.add_paragraph()
        p_d.text = desc
        p_d.font.name = FONT_BODY
        p_d.font.size = Pt(9.5)
        p_d.font.color.rgb = COLOR_MUTED

bot_bar = create_card(slide4, Inches(0.8), Inches(6.15), Inches(11.733), Inches(0.6), bg_color=COLOR_CARD_SUBTLE, border_color=COLOR_BORDER)
tb_bb = slide4.shapes.add_textbox(Inches(1.0), Inches(6.22), Inches(11.333), Inches(0.45))
tf_bb = tb_bb.text_frame
tf_bb.word_wrap = True
tf_bb.margin_left = tf_bb.margin_top = tf_bb.margin_right = tf_bb.margin_bottom = 0
p = tf_bb.paragraphs[0]
p.text = "PIPELINE FLOW: Ingress Request → AI Gateway → Scanner Pipeline → Risk Scoring → Policy Action (Allow / Review / Block) → LLM Execution → Quality Audit → Asynchronous Kafka Event"
p.font.name = FONT_MONO
p.font.size = Pt(9.5)
p.font.bold = True
p.font.color.rgb = COLOR_PRIMARY

add_footer(slide4, 4, pitch_cue="Note: Hexagonal architecture means individual security detectors can scale independently without bottlenecking the main gateway.")

# ==============================================================================
# SLIDE 5: FUNCTIONAL FLOW (8-STAGE PIPELINE)
# ==============================================================================
slide5 = prs.slides.add_slide(blank_layout)
add_slide_background(slide5)
add_header(slide5, "Execution Telemetry", "End-to-End Functional Flow: 8-Stage Pipeline", "A deterministic inspection pipeline ensuring zero unvetted prompts reach external providers.", kpi_chip="SUB-50MS EXECUTION")

stages = [
    ("1. User Auth & Context", "Validates caller JWT token, user identity, role clearance (Intern, Engineer, Admin), and department budget."),
    ("2. Prompt Ingestion & Scanning", "Parallel lexical & regex token inspection checking for credential patterns, API keys, confidential tags, and jailbreaks."),
    ("3. Data Classification", "Categorizes input payload (PII, Financial, Source Code, Credentials, Safe) and assigns data sensitivity weighting."),
    ("4. Risk Scoring Engine", "Calculates composite risk score (0-100) using weighted multi-factor heuristic matrix across role, category, and target model."),
    ("5. Decision Gate Enforcement", "Evaluates risk against organizational threshold: ALLOW (auto-redact if needed), REVIEW (queue for human approval), or BLOCK."),
    ("6. Upstream LLM Execution", "Dispatches sanitized payload to frontier LLM (OpenAI, Gemini, Claude) or returns deterministic governed mock output."),
    ("7. Response Validation", "Inspects generated LLM completion for hallucination probability, toxic output, policy violation, or accidental secret leakage."),
    ("8. Asynchronous Audit Logging", "Dispatches complete end-to-end execution metadata to Kafka & local H2 datastore for compliance recording and tracing.")
]

c_w = Inches(5.7)
c_h = Inches(1.1)
c_gap_y = Inches(0.16)

for idx, (title, desc) in enumerate(stages):
    col = idx // 4
    row = idx % 4
    left = Inches(0.8) + col * (c_w + Inches(0.333))
    top = Inches(1.85) + row * (c_h + c_gap_y)
    
    card = create_card(slide5, left, top, c_w, c_h, bg_color=COLOR_CARD, border_color=COLOR_BORDER)
    
    tb = slide5.shapes.add_textbox(left + Inches(0.2), top + Inches(0.12), c_w - Inches(0.4), c_h - Inches(0.24))
    tf = tb.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0

    p_t = tf.paragraphs[0]
    p_t.text = title
    p_t.font.name = FONT_HEADING
    p_t.font.size = Pt(11)
    p_t.font.bold = True
    p_t.font.color.rgb = COLOR_PRIMARY
    p_t.space_after = Pt(2)

    p_d = tf.add_paragraph()
    p_d.text = desc
    p_d.font.name = FONT_BODY
    p_d.font.size = Pt(9.5)
    p_d.font.color.rgb = COLOR_TEXT

add_footer(slide5, 5, pitch_cue="Walk through the pipeline: Steps 1-5 happen pre-call; Step 6 is execution; Steps 7-8 happen post-call before user receives answer.")

# ==============================================================================
# SLIDE 6: PROMPT SECURITY SCANNER
# ==============================================================================
slide6 = prs.slides.add_slide(blank_layout)
add_slide_background(slide6)
add_header(slide6, "Threat Detection Engine", "Prompt Security Scanner: Multi-Vector Interception", "Zero-day jailbreak defense and microsecond pattern matching for credentials and PII.", kpi_chip="99.8% DETECTION ACCURACY")

c_left = create_card(slide6, Inches(0.8), Inches(1.85), Inches(5.1), Inches(4.8))
tb_l = slide6.shapes.add_textbox(Inches(1.05), Inches(2.1), Inches(4.6), Inches(4.3))
tf_l = tb_l.text_frame
tf_l.word_wrap = True
tf_l.margin_left = tf_l.margin_top = tf_l.margin_right = tf_l.margin_bottom = 0

p = tf_l.paragraphs[0]
p.text = "5 PARALLEL SCANNING ENGINES"
p.font.name = FONT_HEADING
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = COLOR_PRIMARY
p.space_after = Pt(8)

scanner_bullets = [
    ("API Keys & Cloud Credentials", "AWS access keys, OpenAI keys, GCP service accounts, HuggingFace tokens, GitHub PATs."),
    ("Passwords & Private Keys", "Database connection strings, RSA/SSH private keys, authentication passwords, certificates."),
    ("Personally Identifiable Information (PII)", "Social Security Numbers, credit cards, bank accounts, emails, personal phone records."),
    ("Adversarial Prompt Injections", "Bypass instructions, jailbreaks, system instruction overrides, DAN exploits, roleplay bypasses."),
    ("Proprietary Code & Confidential Data", "Core algorithmic logic, internal architecture schemas, trade secrets, confidential tags.")
]

for title, desc in scanner_bullets:
    p_t = tf_l.add_paragraph()
    p_t.text = f"• {title}"
    p_t.font.name = FONT_HEADING
    p_t.font.size = Pt(11)
    p_t.font.bold = True
    p_t.font.color.rgb = COLOR_TEXT
    p_t.space_before = Pt(4)
    
    p_d = tf_l.add_paragraph()
    p_d.text = f"   {desc}"
    p_d.font.name = FONT_BODY
    p_d.font.size = Pt(9.5)
    p_d.font.color.rgb = COLOR_MUTED
    p_d.space_after = Pt(3)

create_screenshot_placeholder(
    slide6, Inches(6.2), Inches(1.85), Inches(6.333), Inches(4.8),
    "Prompt Security Scanner Console",
    "Display UI view of Prompt Security Scanner detecting PII, credentials, and injection attempts with flagged risk scores."
)

add_footer(slide6, 6, pitch_cue="Demonstrate the scanner: It doesn't rely solely on basic keywords; it uses structural syntax and behavioral pattern heuristics.")

# ==============================================================================
# SLIDE 7: AUTO REDACTION
# ==============================================================================
slide7 = prs.slides.add_slide(blank_layout)
add_slide_background(slide7)
add_header(slide7, "Preserving Productivity", "Auto Redaction: Data Sanitization Without Blocking", "Masking sensitive tokens dynamically so engineering workflows proceed without disruption.", kpi_chip="ZERO WORKFLOW BLOCKING")

c_left = create_card(slide7, Inches(0.8), Inches(1.85), Inches(5.1), Inches(4.8))
tb_l = slide7.shapes.add_textbox(Inches(1.05), Inches(2.1), Inches(4.6), Inches(4.3))
tf_l = tb_l.text_frame
tf_l.word_wrap = True
tf_l.margin_left = tf_l.margin_top = tf_l.margin_right = tf_l.margin_bottom = 0

p = tf_l.paragraphs[0]
p.text = "INTELLIGENT VALUE PRESERVATION"
p.font.name = FONT_HEADING
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = COLOR_PRIMARY
p.space_after = Pt(6)

p = tf_l.add_paragraph()
p.text = "A major flaw in traditional security tools is blocking queries outright. Guardian's auto-redaction sanitizes credentials and PII on-the-fly, allowing models to answer technical questions safely."
p.font.name = FONT_BODY
p.font.size = Pt(11)
p.font.color.rgb = COLOR_TEXT
p.space_after = Pt(12)

before_card = create_card(slide7, Inches(1.05), Inches(3.2), Inches(4.6), Inches(1.4), bg_color=COLOR_CARD_SUBTLE, border_color=COLOR_BORDER)
tb_b = slide7.shapes.add_textbox(Inches(1.2), Inches(3.3), Inches(4.3), Inches(1.2))
tf_b = tb_b.text_frame
tf_b.word_wrap = True
tf_b.margin_left = tf_b.margin_top = tf_b.margin_right = tf_b.margin_bottom = 0
p = tf_b.paragraphs[0]
p.text = "BEFORE (RAW INGRESS - HIGH RISK):"
p.font.name = FONT_HEADING
p.font.size = Pt(9.5)
p.font.bold = True
p.font.color.rgb = COLOR_CRITICAL
p.space_after = Pt(4)
p = tf_b.add_paragraph()
p.text = "SSN: 123-45-6789 | Card: 4111-2222-3333-4444\nAPI_KEY: sk-live-992817293847291028374829\npassword=SecretPassword123!"
p.font.name = FONT_MONO
p.font.size = Pt(9)
p.font.color.rgb = COLOR_TEXT

after_card = create_card(slide7, Inches(1.05), Inches(4.8), Inches(4.6), Inches(1.4), bg_color=COLOR_CARD_ACCENT, border_color=COLOR_GREEN)
tb_a = slide7.shapes.add_textbox(Inches(1.2), Inches(4.9), Inches(4.3), Inches(1.2))
tf_a = tb_a.text_frame
tf_a.word_wrap = True
tf_a.margin_left = tf_a.margin_top = tf_a.margin_right = tf_a.margin_bottom = 0
p = tf_a.paragraphs[0]
p.text = "AFTER (SANITIZED - MODEL READY):"
p.font.name = FONT_HEADING
p.font.size = Pt(9.5)
p.font.bold = True
p.font.color.rgb = COLOR_GREEN
p.space_after = Pt(4)
p = tf_a.add_paragraph()
p.text = "SSN: [REDACTED_SSN] | Card: [REDACTED_CARD]\nAPI_KEY: [REDACTED_OPENAI_KEY]\npassword=[REDACTED_SECRET]"
p.font.name = FONT_MONO
p.font.size = Pt(9)
p.font.color.rgb = COLOR_TEXT

create_screenshot_placeholder(
    slide7, Inches(6.2), Inches(1.85), Inches(6.333), Inches(4.8),
    "Redaction Engine & Masked Prompt Inspector",
    "Display playground redacted view tab illustrating side-by-side original prompt vs masked tokens ([REDACTED])."
)

add_footer(slide7, 7, pitch_cue="Crucial Jury Point: This solves the developer friction problem. Developers get their answers, SecOps gets 100% compliance.")

# ==============================================================================
# SLIDE 8: RISK SCORING ENGINE
# ==============================================================================
slide8 = prs.slides.add_slide(blank_layout)
add_slide_background(slide8)
add_header(slide8, "Quantitative Threat Quantification", "Risk Scoring Engine: Multi-Factor Heuristic Matrix", "Evaluating data sensitivity, user clearance, department policy, and model risk in real time.", kpi_chip="DYNAMIC 0-100 INDEX")

c_left = create_card(slide8, Inches(0.8), Inches(1.85), Inches(5.1), Inches(4.8))
tb_l = slide8.shapes.add_textbox(Inches(1.05), Inches(2.1), Inches(4.6), Inches(4.3))
tf_l = tb_l.text_frame
tf_l.word_wrap = True
tf_l.margin_left = tf_l.margin_top = tf_l.margin_right = tf_l.margin_bottom = 0

p = tf_l.paragraphs[0]
p.text = "WEIGHTED 5-FACTOR ALGORITHM"
p.font.name = FONT_HEADING
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = COLOR_PRIMARY
p.space_after = Pt(6)

factors = [
    ("Data Sensitivity (Weight: 35%)", "Severity of detected entities (Confidential, PII, Secrets, Source Code)."),
    ("User Role & Clearance (Weight: 20%)", "Principal privilege level (Intern vs Engineer vs Principal Architect)."),
    ("Department Governance (Weight: 15%)", "Department budget, policy strictness tier, and compliance posture."),
    ("Prompt Intent Category (Weight: 20%)", "Heuristic classification (Code Debugging, General Query, HR, Finance)."),
    ("Target Model Profile (Weight: 10%)", "External commercial cloud LLM vs internal on-premise hosted model.")
]

for title, desc in factors:
    p_t = tf_l.add_paragraph()
    p_t.text = f"• {title}"
    p_t.font.name = FONT_HEADING
    p_t.font.size = Pt(10.5)
    p_t.font.bold = True
    p_t.font.color.rgb = COLOR_TEXT
    p_t.space_before = Pt(3)
    p_d = tf_l.add_paragraph()
    p_d.text = f"   {desc}"
    p_d.font.name = FONT_BODY
    p_d.font.size = Pt(9.5)
    p_d.font.color.rgb = COLOR_MUTED

p_tier = tf_l.add_paragraph()
p_tier.text = "4 OPERATIONAL RISK TIERS:"
p_tier.font.name = FONT_HEADING
p_tier.font.size = Pt(10)
p_tier.font.bold = True
p_tier.font.color.rgb = COLOR_PRIMARY
p_tier.space_before = Pt(8)

p_t_vals = tf_l.add_paragraph()
p_t_vals.text = "LOW (0-29)  |  MEDIUM (30-59)  |  HIGH (60-79)  |  CRITICAL (80-100)"
p_t_vals.font.name = FONT_MONO
p_t_vals.font.size = Pt(9.5)
p_t_vals.font.bold = True
p_t_vals.font.color.rgb = COLOR_TEXT

create_screenshot_placeholder(
    slide8, Inches(6.2), Inches(1.85), Inches(6.333), Inches(4.8),
    "Risk Engine Breakdown & Factor Scoring Matrix",
    "Display Risk Engine score breakdown UI showing factor contributions and risk score gauge (0-100)."
)

add_footer(slide8, 8, pitch_cue="Explain the weighting: Context matters. A junior intern querying confidential financials gets higher risk than a lead architect.")

# ==============================================================================
# SLIDE 9: AI REQUEST DECISION ENGINE
# ==============================================================================
slide9 = prs.slides.add_slide(blank_layout)
add_slide_background(slide9)
add_header(slide9, "Automated Enforcement", "AI Request Decision Engine: Policy-Driven Actions", "Transforming continuous risk scores into deterministic, auditable governance actions.", kpi_chip="ZERO AMBIGUITY")

c_left = create_card(slide9, Inches(0.8), Inches(1.85), Inches(5.1), Inches(4.8))
tb_l = slide9.shapes.add_textbox(Inches(1.05), Inches(2.1), Inches(4.6), Inches(4.3))
tf_l = tb_l.text_frame
tf_l.word_wrap = True
tf_l.margin_left = tf_l.margin_top = tf_l.margin_right = tf_l.margin_bottom = 0

p = tf_l.paragraphs[0]
p.text = "THREE GOVERNED OUTCOMES"
p.font.name = FONT_HEADING
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = COLOR_PRIMARY
p.space_after = Pt(8)

decisions = [
    ("ALLOW (Score < 60)", "Compliant or safely redacted request. Gateway dispatches query to model immediately with sub-second execution latency.", COLOR_GREEN),
    ("REVIEW (Score 60 - 79)", "High-risk or policy-sensitive query intercepted for human-in-the-loop review. Ticket created in SecOps approval queue with notification.", COLOR_WARNING),
    ("BLOCK (Score >= 80)", "Critical vulnerability, confirmed jailbreak attempt, or unredactable key leak. Gateway immediately terminates request with audit justification.", COLOR_CRITICAL)
]

for title, desc, color in decisions:
    p_t = tf_l.add_paragraph()
    p_t.text = f"■  {title}"
    p_t.font.name = FONT_HEADING
    p_t.font.size = Pt(11.5)
    p_t.font.bold = True
    p_t.font.color.rgb = color
    p_t.space_before = Pt(8)
    
    p_d = tf_l.add_paragraph()
    p_d.text = desc
    p_d.font.name = FONT_BODY
    p_d.font.size = Pt(10)
    p_d.font.color.rgb = COLOR_TEXT
    p_d.space_before = Pt(2)

create_screenshot_placeholder(
    slide9, Inches(6.2), Inches(1.85), Inches(6.333), Inches(4.8),
    "Decision Engine Pipeline Trace & Action Modal",
    "Display UI pipeline trace viewer showcasing decision verdict (ALLOW, REVIEW, or BLOCK) with violation context."
)

add_footer(slide9, 9, pitch_cue="Point out: The thresholds (60, 80) are hot-configurable per department policy without restarting services.")

# ==============================================================================
# SLIDE 10: LLM GOVERNANCE & CATALOG
# ==============================================================================
slide10 = prs.slides.add_slide(blank_layout)
add_slide_background(slide10)
add_header(slide10, "Model Orchestration & FinOps", "LLM Governance: Neutrality, Cost Metering & Mocking", "Unified multi-vendor abstraction preventing vendor lock-in and eliminating runaway AI costs.", kpi_chip="ZERO VENDOR LOCK-IN")

c_left = create_card(slide10, Inches(0.8), Inches(1.85), Inches(5.1), Inches(4.8))
tb_l = slide10.shapes.add_textbox(Inches(1.05), Inches(2.1), Inches(4.6), Inches(4.3))
tf_l = tb_l.text_frame
tf_l.word_wrap = True
tf_l.margin_left = tf_l.margin_top = tf_l.margin_right = tf_l.margin_bottom = 0

p = tf_l.paragraphs[0]
p.text = "UNIFIED MODEL MANAGEMENT"
p.font.name = FONT_HEADING
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = COLOR_PRIMARY
p.space_after = Pt(8)

llm_bullets = [
    ("Vendor-Agnostic Model Routing", "Seamlessly routes across OpenAI (GPT-4o), Anthropic (Claude 3.5), Google (Gemini 1.5), and local enterprise LLMs."),
    ("OpenAI API Standard Protocol", "Zero-code change integration: Drop-in compatible with existing enterprise applications and LangChain/LlamaIndex frameworks."),
    ("Deterministic Mock Provider", "Enables local offline development and automated CI/CD security testing with zero external API costs and 0ms latency."),
    ("Granular Token Accounting", "Captures exact input prompt tokens, output completion tokens, and peak context utilization per transaction."),
    ("Real-Time Financial Cost Metering", "Computes dollar cost per prompt based on live vendor pricing models ($5.00/1M tokens prompt, $15.00/1M completion).")
]

for title, desc in llm_bullets:
    p_t = tf_l.add_paragraph()
    p_t.text = f"• {title}"
    p_t.font.name = FONT_HEADING
    p_t.font.size = Pt(10.5)
    p_t.font.bold = True
    p_t.font.color.rgb = COLOR_TEXT
    p_t.space_before = Pt(3)
    p_d = tf_l.add_paragraph()
    p_d.text = f"   {desc}"
    p_d.font.name = FONT_BODY
    p_d.font.size = Pt(9.5)
    p_d.font.color.rgb = COLOR_MUTED

create_screenshot_placeholder(
    slide10, Inches(6.2), Inches(1.85), Inches(6.333), Inches(4.8),
    "Model Catalog, Token Usage & Cost Analytics",
    "Display UI model governance view showing supported vendor models, pricing tables, and token consumption charts."
)

add_footer(slide10, 10, pitch_cue="Highlight the Mock Provider: Judges love seeing that developers can test full security pipelines locally without incurring API bills.")

# ==============================================================================
# SLIDE 11: RESPONSE QUALITY CHECKER
# ==============================================================================
slide11 = prs.slides.add_slide(blank_layout)
add_slide_background(slide11)
add_header(slide11, "Post-Generation Defense", "Response Quality Checker: Hallucination & Egress Safety", "Continuous egress verification ensuring model output is grounded, safe, and leak-free.", kpi_chip="EGRESS VERIFICATION")

c_left = create_card(slide11, Inches(0.8), Inches(1.85), Inches(5.1), Inches(4.8))
tb_l = slide11.shapes.add_textbox(Inches(1.05), Inches(2.1), Inches(4.6), Inches(4.3))
tf_l = tb_l.text_frame
tf_l.word_wrap = True
tf_l.margin_left = tf_l.margin_top = tf_l.margin_right = tf_l.margin_bottom = 0

p = tf_l.paragraphs[0]
p.text = "EGRESS GUARDRAILS"
p.font.name = FONT_HEADING
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = COLOR_PRIMARY
p.space_after = Pt(8)

quality_checks = [
    ("Hallucination Confidence Scoring", "Statistical evaluation calculating factual grounding and identifying unverified algorithmic output."),
    ("Accidental Credential Regurgitation", "Guarantees model output has not memorized or regurgitated internal keys, passwords, or customer records."),
    ("Toxicity & Policy Infractions", "Automated sentiment and language analysis suppressing hostile, defamatory, or biased completions."),
    ("Compliance Safety Verdict", "Assigns binary PASS / FAIL verdict to completions before payload is delivered to client applications."),
    ("Automated Fail-Safe Fallbacks", "Quarantined toxic or hallucinated responses are automatically substituted with governed corporate notices.")
]

for title, desc in quality_checks:
    p_t = tf_l.add_paragraph()
    p_t.text = f"• {title}"
    p_t.font.name = FONT_HEADING
    p_t.font.size = Pt(10.5)
    p_t.font.bold = True
    p_t.font.color.rgb = COLOR_TEXT
    p_t.space_before = Pt(3)
    p_d = tf_l.add_paragraph()
    p_d.text = f"   {desc}"
    p_d.font.name = FONT_BODY
    p_d.font.size = Pt(9.5)
    p_d.font.color.rgb = COLOR_MUTED

create_screenshot_placeholder(
    slide11, Inches(6.2), Inches(1.85), Inches(6.333), Inches(4.8),
    "Response Quality Checker & Hallucination Score",
    "Display quality evaluation view showing response verdict (PASS/FAIL), toxicity score, and hallucination percentage."
)

add_footer(slide11, 11, pitch_cue="Key Differentiator: Many products only inspect ingress prompts. Guardian inspects BOTH prompt and LLM response.")

# ==============================================================================
# SLIDE 12: AUDIT & COMPLIANCE LEDGER
# ==============================================================================
slide12 = prs.slides.add_slide(blank_layout)
add_slide_background(slide12)
add_header(slide12, "Regulatory Conformance", "Audit & Compliance Ledger: Tamper-Evident History", "Immutable, structured interaction records ready for NIST AI RMF, ISO 42001, and OWASP audits.", kpi_chip="NIST AI RMF COMPLIANT")

c_left = create_card(slide12, Inches(0.8), Inches(1.85), Inches(5.1), Inches(4.8))
tb_l = slide12.shapes.add_textbox(Inches(1.05), Inches(2.1), Inches(4.6), Inches(4.3))
tf_l = tb_l.text_frame
tf_l.word_wrap = True
tf_l.margin_left = tf_l.margin_top = tf_l.margin_right = tf_l.margin_bottom = 0

p = tf_l.paragraphs[0]
p.text = "FULL-FIDELITY AUDIT TRAIL"
p.font.name = FONT_HEADING
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = COLOR_PRIMARY
p.space_after = Pt(8)

p = tf_l.add_paragraph()
p.text = "Every transaction is logged asynchronously through Kafka into persistent storage, capturing complete forensic metadata without degrading gateway latency:"
p.font.name = FONT_BODY
p.font.size = Pt(10.5)
p.font.color.rgb = COLOR_TEXT
p.space_after = Pt(6)

audit_fields = [
    ("User Identity & Department", "Authenticated email, user role, client IP address, and department cost center."),
    ("Prompt Category & Entities", "Classification intent tag and enumerated list of detected sensitive entities."),
    ("Risk Score & Decision Verdict", "Composite risk index (0-100) and authoritative enforcement action (ALLOW/REVIEW/BLOCK)."),
    ("Tokens & Micro-Cost Billing", "Prompt tokens, completion tokens, execution time in ms, and exact dollar cost."),
    ("Compliance Framework Alignment", "Real-time mapping against OWASP Top 10 for LLMs (LLM01-LLM06) and ISO/IEC 42001.")
]

for title, desc in audit_fields:
    p_t = tf_l.add_paragraph()
    p_t.text = f"• {title}:"
    p_t.font.name = FONT_HEADING
    p_t.font.size = Pt(10)
    p_t.font.bold = True
    p_t.font.color.rgb = COLOR_TEXT
    p_d = tf_l.add_paragraph()
    p_d.text = f"   {desc}"
    p_d.font.name = FONT_BODY
    p_d.font.size = Pt(9.5)
    p_d.font.color.rgb = COLOR_MUTED

create_screenshot_placeholder(
    slide12, Inches(6.2), Inches(1.85), Inches(6.333), Inches(4.8),
    "Audit Log Explorer & Compliance Search",
    "Display Audit Log Explorer table showcasing filter tabs (All, Risk Alerts, Blocked, Review), user IDs, risk scores, and timestamps."
)

add_footer(slide12, 12, pitch_cue="Tell the jury: In an enterprise audit, SecOps can export complete forensic proof for any prompt in under 5 seconds.")

# ==============================================================================
# SLIDE 13: EXECUTIVE DASHBOARD
# ==============================================================================
slide13 = prs.slides.add_slide(blank_layout)
add_slide_background(slide13)
add_header(slide13, "Operational Visibility", "Executive Operations Dashboard: Real-Time Governance", "A unified single-pane-of-glass command center for CISOs, SecOps, and IT leaders.", kpi_chip="SINGLE-PANE-OF-GLASS")

c_left = create_card(slide13, Inches(0.8), Inches(1.85), Inches(5.1), Inches(4.8))
tb_l = slide13.shapes.add_textbox(Inches(1.05), Inches(2.1), Inches(4.6), Inches(4.3))
tf_l = tb_l.text_frame
tf_l.word_wrap = True
tf_l.margin_left = tf_l.margin_top = tf_l.margin_right = tf_l.margin_bottom = 0

p = tf_l.paragraphs[0]
p.text = "EXECUTIVE TELEMETRY"
p.font.name = FONT_HEADING
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = COLOR_PRIMARY
p.space_after = Pt(8)

dashboard_kpis = [
    ("Real-Time Ingestion Volume", "Live prompt ingestion rate, peak concurrency, and 24-hour volume trajectory."),
    ("Threat Containment Metrics", "Total blocked attacks, auto-redacted tokens count, and jailbreak prevention ratio."),
    ("Pending Human Approvals", "Immediate visibility into tickets queued for manual human authorization."),
    ("Enterprise AI Spend Meter", "Cumulative financial consumption broken down by department, model, and project."),
    ("Risk Tier Distribution", "Proportional breakdown of enterprise traffic across Low, Medium, High, and Critical.")
]

for title, desc in dashboard_kpis:
    p_t = tf_l.add_paragraph()
    p_t.text = f"• {title}"
    p_t.font.name = FONT_HEADING
    p_t.font.size = Pt(10.5)
    p_t.font.bold = True
    p_t.font.color.rgb = COLOR_TEXT
    p_t.space_before = Pt(3)
    p_d = tf_l.add_paragraph()
    p_d.text = f"   {desc}"
    p_d.font.name = FONT_BODY
    p_d.font.size = Pt(9.5)
    p_d.font.color.rgb = COLOR_MUTED

create_screenshot_placeholder(
    slide13, Inches(6.2), Inches(1.85), Inches(6.333), Inches(4.8),
    "Executive Governance Dashboard",
    "Display primary Operations Dashboard showing 4 top KPI cards (Total Requests, Blocked, Pending, Cost) and 24-Hour Traffic Chart."
)

add_footer(slide13, 13, pitch_cue="Point out the Saffron theme: Clean enterprise design that executive leadership can present directly to boardroom stakeholders.")

# ==============================================================================
# SLIDE 14: RISK ALERTS & INCIDENT TRIAGE
# ==============================================================================
slide14 = prs.slides.add_slide(blank_layout)
add_slide_background(slide14)
add_header(slide14, "SecOps Incident Triage", "Real-Time Risk Alerts & Automated Incident Triage", "Instant alerting on critical breach attempts with one-click trace and policy correlation.", kpi_chip="ZERO-DELAY ALERTS")

c_left = create_card(slide14, Inches(0.8), Inches(1.85), Inches(5.1), Inches(4.8))
tb_l = slide14.shapes.add_textbox(Inches(1.05), Inches(2.1), Inches(4.6), Inches(4.3))
tf_l = tb_l.text_frame
tf_l.word_wrap = True
tf_l.margin_left = tf_l.margin_top = tf_l.margin_right = tf_l.margin_bottom = 0

p = tf_l.paragraphs[0]
p.text = "INCIDENT DETECTION & MITIGATION"
p.font.name = FONT_HEADING
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = COLOR_PRIMARY
p.space_after = Pt(8)

alert_features = [
    ("Critical Event Escalation", "Real-time UI alerts for high-severity jailbreak overrides and root AWS/OpenAI key leaks."),
    ("Policy Infraction Attribution", "Pinpointing exact policy rules breached (e.g. POL-01 Prompt Injection, POL-03 Customer PII)."),
    ("Adversarial Pattern Clustering", "Correlating repeated attack patterns across callers, IP subnets, or compromised tokens."),
    ("One-Click Forensic Drilldown", "Direct link to sanitized prompt trace, detector matches, and raw execution timeline."),
    ("Automated Threat Quarantine", "Configurable auto-isolation of repeat-offender caller tokens or unauthorized API clients.")
]

for title, desc in alert_features:
    p_t = tf_l.add_paragraph()
    p_t.text = f"• {title}"
    p_t.font.name = FONT_HEADING
    p_t.font.size = Pt(10.5)
    p_t.font.bold = True
    p_t.font.color.rgb = COLOR_TEXT
    p_t.space_before = Pt(3)
    p_d = tf_l.add_paragraph()
    p_d.text = f"   {desc}"
    p_d.font.name = FONT_BODY
    p_d.font.size = Pt(9.5)
    p_d.font.color.rgb = COLOR_MUTED

create_screenshot_placeholder(
    slide14, Inches(6.2), Inches(1.85), Inches(6.333), Inches(4.8),
    "Risk Alerts & Incident Notification Center",
    "Display UI notification drop-panel and alerts explorer highlighting Critical, Warning, and Info security events."
)

add_footer(slide14, 14, pitch_cue="Explain the triage: SecOps is not inundated with noisy logs; only statistically anomalous, high-risk items trigger active alerts.")

# ==============================================================================
# SLIDE 15: APPROVAL WORKFLOW
# ==============================================================================
slide15 = prs.slides.add_slide(blank_layout)
add_slide_background(slide15)
add_header(slide15, "Human Governance", "Approval Workflow: Human-in-the-Loop Governance", "Controlled delegation for high-impact queries requiring human authorization.", kpi_chip="CONTROLLED DELEGATION")

c_left = create_card(slide15, Inches(0.8), Inches(1.85), Inches(5.1), Inches(4.8))
tb_l = slide15.shapes.add_textbox(Inches(1.05), Inches(2.1), Inches(4.6), Inches(4.3))
tf_l = tb_l.text_frame
tf_l.word_wrap = True
tf_l.margin_left = tf_l.margin_top = tf_l.margin_right = tf_l.margin_bottom = 0

p = tf_l.paragraphs[0]
p.text = "GOVERNANCE TICKET LIFECYCLE"
p.font.name = FONT_HEADING
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = COLOR_PRIMARY
p.space_after = Pt(8)

p = tf_l.add_paragraph()
p.text = "When an interaction triggers moderate risk (60-79) or exceeds cost thresholds, it is held in a secure pending queue rather than being rejected outright:"
p.font.name = FONT_BODY
p.font.size = Pt(10.5)
p.font.color.rgb = COLOR_TEXT
p.space_after = Pt(8)

steps = [
    ("1. Request Flagged", "Engine assigns REVIEW status based on elevated risk score (60-79) or budget limits."),
    ("2. Queue Routing", "Ticket routed to SecOps architect queue with violation summary and redaction preview."),
    ("3. Triage & Decision", "Authorized reviewer inspects business justification and clicks Approve or Reject."),
    ("4. Execution & Audit", "Approved queries proceed to LLM; decisions are immutably logged with approver ID.")
]

for title, desc in steps:
    p_t = tf_l.add_paragraph()
    p_t.text = f"• {title}"
    p_t.font.name = FONT_HEADING
    p_t.font.size = Pt(10)
    p_t.font.bold = True
    p_t.font.color.rgb = COLOR_TEXT
    p_d = tf_l.add_paragraph()
    p_d.text = f"   {desc}"
    p_d.font.name = FONT_BODY
    p_d.font.size = Pt(9.5)
    p_d.font.color.rgb = COLOR_MUTED

create_screenshot_placeholder(
    slide15, Inches(6.2), Inches(1.85), Inches(6.333), Inches(4.8),
    "SecOps Human Review & Approval Queue",
    "Display Approvals Queue UI showing pending prompt tickets, risk badges, reviewer action buttons (Approve / Reject), and justification notes."
)

add_footer(slide15, 15, pitch_cue="Demonstrate business enablement: Borderline queries aren't killed; authorized managers can approve legitimate edge-case research.")

# ==============================================================================
# SLIDE 16: AI GOVERNANCE CAPABILITIES
# ==============================================================================
slide16 = prs.slides.add_slide(blank_layout)
add_slide_background(slide16)
add_header(slide16, "Comprehensive Administration", "AI Governance Capabilities: 5 Administrative Pillars", "Granular departmental policy controls and centralized enterprise LLM administration.", kpi_chip="ENTERPRISE SCALE")

gov_capabilities = [
    ("Policy Management", "Dynamic creation and live hot-reloading of security guardrail policies, injection thresholds, and compliance rulebooks.", "POLICIES"),
    ("Model Registry", "Centralized registry cataloging permitted LLM providers, endpoint configurations, context window limits, and pricing.", "REGISTRY"),
    ("Prompt Templates", "Enterprise-approved system prompts and sanitized template catalog ensuring consistency and prompt hardening.", "TEMPLATES"),
    ("Department Governance", "Per-department token quotas, monthly financial budgets, role clearances, and permitted model restrictions.", "DEPARTMENTS"),
    ("Approval Rules Matrix", "Configurable multi-tier escalation logic routing high-risk prompts to designated department compliance leads.", "APPROVAL RULES")
]

c_w = Inches(11.733)
c_h = Inches(0.85)
c_gap = Inches(0.12)

for i, (title, desc, badge) in enumerate(gov_capabilities):
    c_top = Inches(1.85) + i * (c_h + c_gap)
    card = create_card(slide16, Inches(0.8), c_top, c_w, c_h, bg_color=COLOR_CARD, border_color=COLOR_BORDER)
    
    tb = slide16.shapes.add_textbox(Inches(1.1), c_top + Inches(0.12), c_w - Inches(0.6), c_h - Inches(0.24))
    tf = tb.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0

    p_t = tf.paragraphs[0]
    p_t.text = f"{title}   "
    p_t.font.name = FONT_HEADING
    p_t.font.size = Pt(11.5)
    p_t.font.bold = True
    p_t.font.color.rgb = COLOR_TEXT

    p_d = tf.add_paragraph()
    p_d.text = desc
    p_d.font.name = FONT_BODY
    p_d.font.size = Pt(10)
    p_d.font.color.rgb = COLOR_MUTED

add_footer(slide16, 16, pitch_cue="Emphasize: Department Governance allows Knox Part, R&D, and HR to maintain different security strictness without code changes.")

# ==============================================================================
# SLIDE 17: ENTERPRISE OBSERVABILITY & HEALTH
# ==============================================================================
slide17 = prs.slides.add_slide(blank_layout)
add_slide_background(slide17)
add_header(slide17, "Full-Stack Reliability", "Enterprise Observability: Telemetry, Tracing & Probes", "Continuous health evaluation and distributed tracing across every microservice hop.", kpi_chip="99.99% UPTIME DESIGN")

obs_cards = [
    ("Real-Time Metrics", "Prometheus-compatible instrumentation capturing requests/sec, error rates, p95/p99 latency, and token consumption rates.", "📊"),
    ("Distributed Tracing", "Microsecond-accurate 9-stage pipeline tracing diagnosing processing overhead, detector latency, and network hops.", "🔍"),
    ("Cost Analytics", "Fine-grained expenditure attribution mapping every dollar spent back to specific models, users, and departments.", "💰"),
    ("Risk Analytics", "Trend analysis tracking organizational threat vectors over time, top violation types, and department risk distributions.", "📈"),
    ("Service Health Probes", "Continuous liveness and readiness monitoring across all 5 backend microservices with automatic failure alerts.", "💓")
]

o_width = Inches(2.2)
o_gap = Inches(0.18)
for i, (title, desc, icon) in enumerate(obs_cards):
    left = Inches(0.8) + i * (o_width + o_gap)
    card = create_card(slide17, left, Inches(1.85), o_width, Inches(4.8), bg_color=COLOR_CARD, border_color=COLOR_BORDER)
    
    tb = slide17.shapes.add_textbox(left + Inches(0.18), Inches(2.1), o_width - Inches(0.36), Inches(4.3))
    tf = tb.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0

    p_icon = tf.paragraphs[0]
    p_icon.alignment = PP_ALIGN.CENTER
    p_icon.text = icon
    p_icon.font.size = Pt(28)
    p_icon.space_after = Pt(12)

    p_t = tf.add_paragraph()
    p_t.alignment = PP_ALIGN.CENTER
    p_t.text = title
    p_t.font.name = FONT_HEADING
    p_t.font.size = Pt(12)
    p_t.font.bold = True
    p_t.font.color.rgb = COLOR_TEXT
    p_t.space_after = Pt(12)

    p_d = tf.add_paragraph()
    p_d.text = desc
    p_d.font.name = FONT_BODY
    p_d.font.size = Pt(10)
    p_d.font.color.rgb = COLOR_MUTED

add_footer(slide17, 17, pitch_cue="Highlight: If an upstream model is slow, the distributed trace proves whether the delay is in the security scanner (<15ms) or external API.")

# ==============================================================================
# SLIDE 18: BUSINESS IMPACT & ROI
# ==============================================================================
slide18 = prs.slides.add_slide(blank_layout)
add_slide_background(slide18)
add_header(slide18, "Quantified Value Creation", "Tangible Business Impact: Security, Cost & Speed", "Unlocking enterprise-wide generative AI adoption with proven ROI across four dimensions.", kpi_chip="HIGH ROI ACCELERATOR")

impact_pillars = [
    ("Security & IP Defense", "100% Prevention of Critical IP Leaks", "Guarantees zero accidental exfiltration of proprietary source code, secrets, or confidential documents. Completely neutralizes adversarial prompt injection exploits before model ingestion.", COLOR_CRITICAL),
    ("Regulatory Compliance", "Audit-Ready in Under 5 Minutes", "Continuous automated compliance with NIST AI RMF, ISO/IEC 42001, and OWASP Top 10 for LLMs. Generates tamper-proof audit trails for internal & external regulators with zero manual gathering.", COLOR_PRIMARY),
    ("Financial Cost Control", "30-40% Reduction in Runaway AI Bills", "Eliminates surprise cloud billing through department-level rate limits, token quotas, and intelligent model routing (e.g. routing simple queries to efficient models instead of expensive frontier models).", COLOR_WARNING),
    ("Safe & Accelerated Adoption", "Frictionless AI Innovation", "Unlocks enterprise-wide AI productivity by providing security and compliance leadership the confidence to enable frontier models across all departments without bureaucratic delays.", COLOR_GREEN)
]

c_w = Inches(5.7)
c_h = Inches(2.3)
c_gap_y = Inches(0.2)

for i, (title, sub, desc, color) in enumerate(impact_pillars):
    col = i % 2
    row = i // 2
    left = Inches(0.8) + col * (c_w + Inches(0.333))
    top = Inches(1.85) + row * (c_h + c_gap_y)
    
    card = create_card(slide18, left, top, c_w, c_h, bg_color=COLOR_CARD, border_color=COLOR_BORDER)
    
    tb = slide18.shapes.add_textbox(left + Inches(0.3), top + Inches(0.2), c_w - Inches(0.6), c_h - Inches(0.4))
    tf = tb.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0

    p_t = tf.paragraphs[0]
    p_t.text = title
    p_t.font.name = FONT_HEADING
    p_t.font.size = Pt(13)
    p_t.font.bold = True
    p_t.font.color.rgb = color
    p_t.space_after = Pt(2)

    p_s = tf.add_paragraph()
    p_s.text = sub
    p_s.font.name = FONT_HEADING
    p_s.font.size = Pt(10.5)
    p_s.font.bold = True
    p_s.font.color.rgb = COLOR_TEXT
    p_s.space_after = Pt(6)

    p_d = tf.add_paragraph()
    p_d.text = desc
    p_d.font.name = FONT_BODY
    p_d.font.size = Pt(10)
    p_d.font.color.rgb = COLOR_MUTED

add_footer(slide18, 18, pitch_cue="Pitch Focus: This is the core slide for business executives. It proves Guardian is a business enabler, not an IT bottleneck.")

# ==============================================================================
# SLIDE 19: TECHNOLOGY STACK
# ==============================================================================
slide19 = prs.slides.add_slide(blank_layout)
add_slide_background(slide19)
add_header(slide19, "Engineering Excellence", "Modern Technology Stack: Lightweight & Scalable", "Pure Java 17 + Spring Boot 3.3 reactive stack engineered for low latency and zero deployment friction.", kpi_chip="JAVA 17 + REACT 18")

tech_categories = [
    ("Backend Core", "Java 17 LTS\nSpring Boot 3.3.5\nSpring WebFlux (Reactive)\nHexagonal Architecture", "☕"),
    ("Event Streaming", "Apache Kafka\nSpring Kafka Streams\nDistributed Audit Topics\nDead Letter Queues", "📨"),
    ("Data Persistence", "PostgreSQL (Enterprise)\nH2 Embedded (Zero-Config)\nSpring Data JPA\nFlyway Schema Migrations", "💾"),
    ("Frontend Dashboard", "React 18 SPA\nVite Fast Bundler\nVanilla CSS Design System\nRecharts Telemetry", "⚛️"),
    ("Observability & Sec", "Spring Actuator\nPrometheus Metrics\nDistributed Trace Logs\nJWT & AES-256 Crypto", "🛡️")
]

t_width = Inches(2.2)
t_gap = Inches(0.18)
for i, (title, details, icon) in enumerate(tech_categories):
    left = Inches(0.8) + i * (t_width + t_gap)
    card = create_card(slide19, left, Inches(1.85), t_width, Inches(4.8), bg_color=COLOR_CARD, border_color=COLOR_BORDER)
    
    tb = slide19.shapes.add_textbox(left + Inches(0.18), Inches(2.1), t_width - Inches(0.36), Inches(4.3))
    tf = tb.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0

    p_icon = tf.paragraphs[0]
    p_icon.alignment = PP_ALIGN.CENTER
    p_icon.text = icon
    p_icon.font.size = Pt(28)
    p_icon.space_after = Pt(12)

    p_t = tf.add_paragraph()
    p_t.alignment = PP_ALIGN.CENTER
    p_t.text = title
    p_t.font.name = FONT_HEADING
    p_t.font.size = Pt(12)
    p_t.font.bold = True
    p_t.font.color.rgb = COLOR_TEXT
    p_t.space_after = Pt(14)

    p_d = tf.add_paragraph()
    p_d.alignment = PP_ALIGN.CENTER
    p_d.text = details
    p_d.font.name = FONT_MONO
    p_d.font.size = Pt(9.5)
    p_d.font.color.rgb = COLOR_MUTED

add_footer(slide19, 19, pitch_cue="Engineering Highlight: Runs locally in seconds without requiring heavy Kubernetes or Docker setups, but scales into cloud clusters seamlessly.")

# ==============================================================================
# SLIDE 20: STRATEGIC ROADMAP
# ==============================================================================
slide20 = prs.slides.add_slide(blank_layout)
add_slide_background(slide20)
add_header(slide20, "Vision & Execution", "Strategic Product Roadmap: Evolutionary Maturity", "A pragmatic three-phase growth trajectory advancing from gateway controls to autonomous governance.", kpi_chip="PHASED EXPANSION")

phases = [
    ("Phase 1: Foundation (Current)", "Inline Gateway & Security Controls", [
        "Inline WebClient AI Gateway Proxy",
        "Deterministic Prompt Security Scanner",
        "Weighted Multi-Factor Risk Scoring Engine",
        "Automated Token Redaction Engine",
        "H2/Kafka Audit Logging & Trace Replay"
    ], COLOR_PRIMARY),
    ("Phase 2: Expansion (Q3-Q4 '26)", "Enterprise Governance Platform", [
        "Department-level Quota & Budget Manager",
        "Fine-Grained RBAC & Dynamic Access Tokens",
        "SecOps Human-in-the-Loop Approval Queue",
        "Pre-approved System Prompt Template Registry",
        "Automated Compliance Report Export (PDF/CSV)"
    ], COLOR_WARNING),
    ("Phase 3: Scale (2027)", "Autonomous Enterprise Scale", [
        "Kubernetes Multi-Region Mesh Deployment",
        "Self-Learning Adversarial Threat Detectors",
        "Hardware Security Module (HSM) Vault Integration",
        "Agentic Autonomous Workflow Verification",
        "Cross-Organization Federated Threat Sharing"
    ], COLOR_GREEN)
]

p_width = Inches(3.68)
p_gap = Inches(0.34)
for i, (phase_title, subtitle, bullets, color) in enumerate(phases):
    left = Inches(0.8) + i * (p_width + p_gap)
    card = create_card(slide20, left, Inches(1.85), p_width, Inches(4.8), bg_color=COLOR_CARD, border_color=COLOR_BORDER)
    
    top_bar = slide20.shapes.add_shape(MSO_SHAPE.RECTANGLE, left, Inches(1.85), p_width, Inches(0.08))
    top_bar.fill.solid()
    top_bar.fill.fore_color.rgb = color
    top_bar.line.fill.background()

    tb = slide20.shapes.add_textbox(left + Inches(0.25), Inches(2.1), p_width - Inches(0.5), Inches(4.3))
    tf = tb.text_frame
    tf.word_wrap = True
    tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0

    p_t = tf.paragraphs[0]
    p_t.text = phase_title
    p_t.font.name = FONT_HEADING
    p_t.font.size = Pt(12.5)
    p_t.font.bold = True
    p_t.font.color.rgb = color
    p_t.space_after = Pt(2)

    p_s = tf.add_paragraph()
    p_s.text = subtitle
    p_s.font.name = FONT_HEADING
    p_s.font.size = Pt(10.5)
    p_s.font.bold = True
    p_s.font.color.rgb = COLOR_TEXT
    p_s.space_after = Pt(14)

    for b in bullets:
        p_b = tf.add_paragraph()
        p_b.text = f"• {b}"
        p_b.font.name = FONT_BODY
        p_b.font.size = Pt(10)
        p_b.font.color.rgb = COLOR_MUTED
        p_b.space_after = Pt(6)

add_footer(slide20, 20, pitch_cue="Demonstrate foresight: Phase 1 is fully functional today; Phase 2 and 3 provide a clear path for enterprise-wide adoption.")

# ==============================================================================
# SLIDE 21: CONCLUSION (FINAL MESSAGE)
# ==============================================================================
slide21 = prs.slides.add_slide(blank_layout)
add_slide_background(slide21)

hero = create_card(slide21, Inches(0.8), Inches(0.7), Inches(11.733), Inches(6.0), bg_color=COLOR_CARD, border_color=COLOR_BORDER)

top_bar21 = slide21.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(0.7), Inches(11.733), Inches(0.12))
top_bar21.fill.solid()
top_bar21.fill.fore_color.rgb = COLOR_PRIMARY
top_bar21.line.fill.background()

shield_box21 = slide21.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(5.866), Inches(1.2), Inches(1.6), Inches(1.0))
shield_box21.fill.solid()
shield_box21.fill.fore_color.rgb = COLOR_CARD_ACCENT
shield_box21.line.color.rgb = COLOR_PRIMARY
s_tf21 = shield_box21.text_frame
s_tf21.vertical_anchor = MSO_ANCHOR.MIDDLE
s_p21 = s_tf21.paragraphs[0]
s_p21.alignment = PP_ALIGN.CENTER
s_p21.text = "🛡️ SDS"
s_p21.font.name = FONT_HEADING
s_p21.font.size = Pt(18)
s_p21.font.bold = True
s_p21.font.color.rgb = COLOR_PRIMARY

tb_end = slide21.shapes.add_textbox(Inches(1.5), Inches(2.4), Inches(10.333), Inches(3.8))
tf_e = tb_end.text_frame
tf_e.word_wrap = True
tf_e.margin_left = tf_e.margin_top = tf_e.margin_right = tf_e.margin_bottom = 0

p_e1 = tf_e.paragraphs[0]
p_e1.alignment = PP_ALIGN.CENTER
p_e1.text = "Secure AI Adoption at Enterprise Scale"
p_e1.font.name = FONT_HEADING
p_e1.font.size = Pt(28)
p_e1.font.bold = True
p_e1.font.color.rgb = COLOR_TEXT
p_e1.space_after = Pt(16)

p_e2 = tf_e.add_paragraph()
p_e2.alignment = PP_ALIGN.CENTER
p_e2.text = "“Enterprise AI Guardian enables organizations to innovate boldly with Generative AI,\nwhile keeping intellectual property, regulatory compliance, and cost under complete control.”"
p_e2.font.name = FONT_HEADING
p_e2.font.size = Pt(16)
p_e2.font.bold = True
p_e2.font.color.rgb = COLOR_PRIMARY
p_e2.space_after = Pt(24)

p_e3 = tf_e.add_paragraph()
p_e3.alignment = PP_ALIGN.CENTER
p_e3.text = "Digithon '26  |  AI / AX  |  AI Governance & Security Track"
p_e3.font.name = FONT_HEADING
p_e3.font.size = Pt(12)
p_e3.font.bold = True
p_e3.font.color.rgb = COLOR_MUTED
p_e3.space_after = Pt(4)

p_e4 = tf_e.add_paragraph()
p_e4.alignment = PP_ALIGN.CENTER
p_e4.text = "Submitted by Abdhesh Singh  •  Dept - Knox Part"
p_e4.font.name = FONT_HEADING
p_e4.font.size = Pt(12)
p_e4.font.bold = True
p_e4.font.color.rgb = COLOR_PRIMARY

add_footer(slide21, 21, pitch_cue="Concluding Call to Action: Thank the jury and open the floor for executive Q&A.")

# ==============================================================================
# SAVE PRESENTATION
# ==============================================================================
output_filename = "SDS_Enterprise_AI_Guardian_Executive_Pitch.pptx"
output_path = os.path.join(os.getcwd(), output_filename)
prs.save(output_path)
print(f"[SUCCESS] Upgraded Presentation generated successfully: {output_path}")

# Also try writing back to the original filename if unlocked
try:
    alt_path = os.path.join(os.getcwd(), "SDS_Enterprise_AI_Guardian_Presentation.pptx")
    prs.save(alt_path)
    print(f"[SUCCESS] Also updated: {alt_path}")
except Exception as e:
    print(f"[NOTE] Original file was locked by an active viewer; executive pitch saved as {output_filename}")

print(f"[TOTAL SLIDES] {len(prs.slides)} slides generated.")
