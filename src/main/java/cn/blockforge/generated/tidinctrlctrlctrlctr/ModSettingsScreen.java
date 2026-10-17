package cn.blockforge.generated.tidinctrlctrlctrlctr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/**
 * 全模组设置页：左侧参数名、中间控件、右侧「重置」，底部「取消 / 完成并退出」。
 *
 * <p>按「基本只放开关、高级只放精度」分工：
 * <ul>
 *   <li><b>基本选项</b>：每个功能一个开 / 关，一眼看清现在开着什么；</li>
 *   <li><b>高级选项</b>：滑条调数值（旋转速度、收线预判、重投节奏），影响判定精度与手感。</li>
 * </ul>
 *
 * <p>只有「高级选项」的数值会写进 config/ 下的 properties，重启仍然生效；
 * 基本区的开关是会话状态，关掉游戏即恢复默认——这跟模组原本「零配置、装上即用」的取向一致。
 */
public final class ModSettingsScreen extends Screen {
    /** 行高下限 / 上限：窗口矮时压缩行距，保证 14 行都画在面板里。 */
    private static final int ROW_MIN = 13;
    private static final int ROW_MAX = 20;
    private static final int LABEL_W = 132;
    private static final int RESET_W = 40;
    private static final int SECTION_H = 12;
    private static final int SECTION_GAP = 6;

    private static final int SCREEN_DIM = 0x4D000000;
    private static final int PANEL_BG = 0xCC000000;
    private static final int PANEL_BORDER = 0xFF8C8C8C;
    private static final int SEPARATOR = 0x599E9E9E;
    private static final int TEXT_TITLE = 0xFFFFFFFF;
    private static final int TEXT_SECTION = 0xFFE6C35C;
    private static final int TEXT_LABEL = 0xFFE0E0E0;
    private static final int TEXT_HINT = 0xFF8F8F8F;

    private final Screen parent;

    private final List<Text> labelTexts = new ArrayList<>();
    private final List<Integer> labelYs = new ArrayList<>();
    private final List<Text> sectionTexts = new ArrayList<>();
    private final List<Integer> sectionYs = new ArrayList<>();

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int innerX;
    private int innerW;
    private int controlX;
    private int controlW;
    private int resetX;

    /** 「取消」要还原的数值快照。 */
    private int snapshotLead;
    private int snapshotRecast;
    private int snapshotJitter;

    public ModSettingsScreen(Screen parent) {
        super(Text.literal("功能设置"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        snapshotLead = AutoFishController.leadMs();
        snapshotRecast = AutoFishController.recastDelayMs();
        snapshotJitter = AutoFishController.recastJitterPercent();

        labelTexts.clear();
        labelYs.clear();
        sectionTexts.clear();
        sectionYs.clear();
        layoutPanel();

        int rows = 14;
        int titleH = 34;
        int bottomH = 50;
        int usable = panelH - titleH - bottomH - SECTION_H * 2 - SECTION_GAP;
        int unit = Math.max(ROW_MIN, Math.min(ROW_MAX, usable / rows));
        int y = panelY + titleH;

        // ---------------- 基本选项：只放开关 ----------------
        y = section("基本选项", y);
        y = toggleRow("旋转（视角 + 模型）", y, unit,
            SpinController.getMode() == SpinController.Mode.VIEW,
            () -> SpinController.toggle(SpinController.Mode.VIEW), false);
        y = toggleRow("角色旋转（视角不转）", y, unit,
            SpinController.getMode() == SpinController.Mode.MODEL,
            () -> SpinController.toggle(SpinController.Mode.MODEL), false);
        y = toggleRow("抬臂旋转（飞机姿势）", y, unit,
            SpinController.getMode() == SpinController.Mode.AIRPLANE,
            () -> SpinController.toggle(SpinController.Mode.AIRPLANE), false);
        y = toggleRow("人物模型隐身", y, unit,
            ModelHideController.isHidden(),
            ModelHideController::toggle, false);
        y = toggleRow("盘腿坐", y, unit,
            SitPoseController.isSitting(),
            SitPoseController::toggle, false);
        y = toggleRow("转R6 比例", y, unit,
            R6ModelController.isEnabled(),
            R6ModelController::toggle, false);
        y = toggleRow("自动钓鱼", y, unit,
            AutoFishController.isEnabled(),
            AutoFishController::toggle, false);
        y = toggleRow("呼出音效", y, unit,
            FunctionMenuMod.soundEnabled,
            () -> FunctionMenuMod.soundEnabled = !FunctionMenuMod.soundEnabled, true);
        y = toggleRow("信息浮层", y, unit,
            FunctionMenuMod.infoOverlayEnabled,
            () -> FunctionMenuMod.infoOverlayEnabled = !FunctionMenuMod.infoOverlayEnabled, false);

        // ---------------- 高级选项：只放精度 ----------------
        y = section("高级选项", y + SECTION_GAP);
        y = sliderRow("旋转速度", y, unit, 0, 3, 1,
            SpinController.speedIndex(), 1,
            ModSettingsScreen::spinSpeedText, SpinController::setSpeedIndex);
        y = directionRow("旋转方向", y, unit);
        y = sliderRow("收线预判", y, unit, 0, 200, 5,
            AutoFishController.leadMs(), 40,
            value -> value + " 毫秒", AutoFishController::setLeadMs);
        y = sliderRow("重新投掷延迟", y, unit, 200, 5000, 50,
            AutoFishController.recastDelayMs(), 1500,
            value -> value + " 毫秒", AutoFishController::setRecastDelayMs);
        y = sliderRow("随机重投延迟", y, unit, 0, 100, 5,
            AutoFishController.recastJitterPercent(), 0,
            value -> "百分之 " + value, AutoFishController::setRecastJitterPercent);

        // ---------------- 底部 ----------------
        int bw = 122;
        int gap = 10;
        int by = panelY + panelH - 32;
        int totalW = bw * 2 + gap;
        int bx = panelX + (panelW - totalW) / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("取消"), b -> {
            AutoFishController.setLeadMs(snapshotLead);
            AutoFishController.setRecastDelayMs(snapshotRecast);
            AutoFishController.setRecastJitterPercent(snapshotJitter);
            closeToParent();
        }).dimensions(bx, by, bw, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("完成并退出"), b -> {
            AutoFishController.saveConfig();
            closeToParent();
        }).dimensions(bx + bw + gap, by, bw, 20).build());
    }

    // -----------------------------------------------------------------------
    // 行的构造
    // -----------------------------------------------------------------------

    private void layoutPanel() {
        panelW = Math.min(520, Math.max(340, (int) Math.round(this.width * 0.58)));
        panelH = Math.min(this.height - 16, Math.max(240, (int) Math.round(this.height * 0.86)));
        panelX = (this.width - panelW) / 2;
        panelY = (this.height - panelH) / 2;
        innerX = panelX + 20;
        innerW = panelW - 40;
        resetX = innerX + innerW - RESET_W;
        controlX = innerX + LABEL_W;
        controlW = Math.max(80, resetX - controlX - 10);
    }

    private int section(String title, int y) {
        sectionTexts.add(Text.literal("− " + title));
        sectionYs.add(y);
        return y + SECTION_H;
    }

    /** 开关行。{@code defaultOn} 是「重置」按钮要恢复到的默认状态。 */
    private int toggleRow(String label, int y, int unit, boolean on, Runnable flip, boolean defaultOn) {
        labelTexts.add(Text.literal(label));
        labelYs.add(y);
        int h = Math.min(20, unit);
        addDrawableChild(ButtonWidget.builder(Text.literal(on ? "开" : "关"), b -> {
            flip.run();
            clearAndInit();
        }).dimensions(controlX, y, controlW, h).build());
        // 已经是默认值就什么都不做，否则翻一下（flip 是开关，等价于设回默认）
        Runnable reset = (on == defaultOn) ? () -> { } : flip;
        addDrawableChild(ButtonWidget.builder(Text.literal("重置"), b -> {
            reset.run();
            clearAndInit();
        }).dimensions(resetX, y, RESET_W, h).build());
        return y + unit;
    }

    /** 数值行：滑条 + 重置到默认值。 */
    private int sliderRow(String label, int y, int unit, int min, int max, int step,
                          int current, int defaultValue,
                          IntFunction<String> format, IntConsumer onChanged) {
        labelTexts.add(Text.literal(label));
        labelYs.add(y);
        int h = Math.min(20, unit);
        addDrawableChild(new IntSlider(controlX, y, controlW, h, min, max, step, current, format, onChanged));
        addDrawableChild(ButtonWidget.builder(Text.literal("重置"), b -> {
            onChanged.accept(defaultValue);
            clearAndInit();
        }).dimensions(resetX, y, RESET_W, h).build());
        return y + unit;
    }

    private int directionRow(String label, int y, int unit) {
        labelTexts.add(Text.literal(label));
        labelYs.add(y);
        int h = Math.min(20, unit);
        addDrawableChild(ButtonWidget.builder(
            Text.literal(SpinController.isClockwise() ? "顺时针" : "逆时针"),
            b -> {
                SpinController.toggleDirection();
                clearAndInit();
            }).dimensions(controlX, y, controlW, h).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("重置"), b -> {
            SpinController.setClockwise(true);
            clearAndInit();
        }).dimensions(resetX, y, RESET_W, h).build());
        return y + unit;
    }

    /** 旋转档位下标 → 「正常 · 180°/秒」这种可读文本。 */
    private static String spinSpeedText(int index) {
        String[] keys = {"slow", "normal", "fast", "ultra"};
        String[] degrees = {"60", "180", "360", "720"};
        int i = Math.max(0, Math.min(keys.length - 1, index));
        return Text.translatable("tidinmenu.spin.speed." + keys[i]).getString() + " · " + degrees[i] + "°/秒";
    }

    // -----------------------------------------------------------------------
    // 绘制 / 生命周期
    // -----------------------------------------------------------------------

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, this.width, this.height, SCREEN_DIM);
        context.fill(panelX - 1, panelY - 1, panelX + panelW + 1, panelY + panelH + 1, PANEL_BORDER);
        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL_BG);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, panelY + 12, TEXT_TITLE);
        context.fill(panelX + 12, panelY + 26, panelX + panelW - 12, panelY + 27, SEPARATOR);

        for (int i = 0; i < sectionTexts.size(); i++) {
            context.drawTextWithShadow(this.textRenderer, sectionTexts.get(i), innerX, sectionYs.get(i), TEXT_SECTION);
        }
        for (int i = 0; i < labelTexts.size(); i++) {
            context.drawTextWithShadow(this.textRenderer, labelTexts.get(i), innerX, labelYs.get(i) + 6, TEXT_LABEL);
        }

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer,
            Text.literal("只有「高级选项」的数值会保存；开关是会话状态，重启即恢复默认"),
            this.width / 2, panelY + panelH - 46, TEXT_HINT);
    }

    @Override
    public void renderBackground(DrawContext context) {
        // 背景由 render() 自己画，不要原版的泥土与模糊
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    /** Esc 与「完成并退出」等价：保存数值后回到上一页。 */
    @Override
    public void close() {
        AutoFishController.saveConfig();
        closeToParent();
    }

    private void closeToParent() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    // -----------------------------------------------------------------------
    // 整数滑条
    // -----------------------------------------------------------------------

    private static final class IntSlider extends SliderWidget {
        private final int min;
        private final int max;
        private final int step;
        private final IntFunction<String> format;
        private final IntConsumer onChanged;

        IntSlider(int x, int y, int width, int height, int min, int max, int step, int current,
                  IntFunction<String> format, IntConsumer onChanged) {
            super(x, y, width, height, Text.literal(""));
            this.min = min;
            this.max = max;
            this.step = step;
            this.format = format;
            this.onChanged = onChanged;
            this.value = max > min ? (double) (current - min) / (double) (max - min) : 0.0;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            // 父类构造期也会调用一次，那时字段还没赋值，必须挡掉
            if (format == null) {
                return;
            }
            setMessage(Text.literal(format.apply(currentValue())));
        }

        @Override
        protected void applyValue() {
            onChanged.accept(currentValue());
        }

        private int currentValue() {
            if (max <= min) {
                return min;
            }
            int raw = (int) Math.round(min + (max - min) * this.value);
            if (step > 1) {
                raw = min + Math.round((float) (raw - min) / step) * step;
            }
            return Math.max(min, Math.min(max, raw));
        }
    }
}
