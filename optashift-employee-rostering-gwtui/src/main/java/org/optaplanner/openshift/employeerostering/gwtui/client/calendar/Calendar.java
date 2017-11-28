package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.MouseEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Panel;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.ConstantFetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.DataProvider;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Fetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;

public class Calendar<G extends HasTitle, I extends HasTimeslot<G>> {

    HTMLCanvasElement canvas;
    CalendarView<G, I> view;
    Collection<I> shifts;
    Integer tenantId;
    Panel topPanel;
    Panel bottomPanel;
    Panel sidePanel;
    Fetchable<Collection<I>> dataProvider;
    Fetchable<List<G>> groupProvider;
    DataProvider<G, I> instanceCreator;

    private Calendar(HTMLCanvasElement canvasElement, Integer tenantId, Panel topPanel, Panel bottomPanel,
            Panel sidePanel, Fetchable<Collection<I>> dataProvider, Fetchable<List<G>> groupProvider, DataProvider<G,
                    I> instanceCreator) {
        this.canvas = canvasElement;
        this.tenantId = tenantId;
        this.topPanel = topPanel;
        this.bottomPanel = bottomPanel;
        this.sidePanel = sidePanel;

        canvas.draggable = false;
        canvas.style.background = "#FFFFFF";

        canvas.onmousedown = (e) -> {
            onMouseDown((MouseEvent) e);
            return e;
        };
        canvas.onmousemove = (e) -> {
            onMouseMove((MouseEvent) e);
            return e;
        };
        canvas.onmouseup = (e) -> {
            onMouseUp((MouseEvent) e);
            return e;
        };

        shifts = new ArrayList<>();

        setInstanceCreator(instanceCreator);
        setGroupProvider(groupProvider);
        setDataProvider(dataProvider);

        refresh();

        Window.addResizeHandler((e) -> {
            canvas.width = e.getWidth() - canvasElement.offsetLeft - sidePanel.getOffsetWidth() - 100;
            canvas.height = e.getHeight() - canvasElement.offsetTop - topPanel.getOffsetHeight() - bottomPanel
                    .getOffsetHeight() - 100;
            draw();
        });

    }

    private void setView(CalendarView<G, I> view) {
        this.view = view;
    }

    private CalendarView<G, I> getView() {
        return view;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
        groupProvider.fetchData(() -> dataProvider.fetchData(Fetchable.DO_NOTHING));
    }

    public void draw() {
        refresh();
        CanvasRenderingContext2D g = (CanvasRenderingContext2D) (Object) canvas.getContext("2d");
        view.draw(g, canvas.width, canvas.height);
    }

    public void refresh() {
        double width = Window.getClientWidth() - canvas.offsetLeft - sidePanel.getOffsetWidth() - 100;
        double height = Window.getClientHeight() - canvas.offsetTop - topPanel.getOffsetHeight() - bottomPanel
                .getOffsetHeight() - 100;

        canvas.width = width;
        canvas.height = height;
    }

    public void forceUpdate() {
        groupProvider.fetchData(() -> dataProvider.fetchData(() -> draw()));
    }

    public void onMouseDown(MouseEvent e) {
        getView().onMouseDown(e);
    }

    public void onMouseMove(MouseEvent e) {
        getView().onMouseMove(e);
    }

    public void onMouseUp(MouseEvent e) {
        getView().onMouseUp(e);
    }

    public void setDate(LocalDateTime date) {
        view.setDate(date);
        dataProvider.fetchData(() -> {
        });
    }

    public LocalDateTime getViewStartDate() {
        return view.getViewStartDate();
    }

    public LocalDateTime getViewEndDate() {
        return view.getViewEndDate();
    }

    public Collection<I> getShifts() {
        return shifts;
    }

    public Collection<G> getGroups() {
        return view.getGroups();
    }

    public Collection<G> getVisibleGroups() {
        return view.getVisibleGroups();
    }

    public void addShift(I shift) {
        shifts.add(shift);
        getView().setShifts(shifts);
        draw();
    }

    public void setDataProvider(Fetchable<Collection<I>> dataProvider) {
        if (null == dataProvider) {
            dataProvider = new ConstantFetchable<>(Collections.emptyList());
        }
        this.dataProvider = dataProvider;
        dataProvider.setUpdatable((d) -> {
            shifts = new ArrayList<>(d);
            getView().setShifts(shifts);
        });
    }

    public void setGroupProvider(Fetchable<List<G>> groupProvider) {
        if (null == groupProvider) {
            groupProvider = new ConstantFetchable<>(Collections.emptyList());
        }
        this.groupProvider = groupProvider;
        groupProvider.setUpdatable((groups) -> getView().setGroups(groups));
    }

    public void setInstanceCreator(DataProvider<G, I> instanceCreator) {
        if (null == instanceCreator) {
            instanceCreator = (c, g, s, e) -> {
            };
        }
        this.instanceCreator = instanceCreator;
    }

    public void addShift(G group, LocalDateTime start, LocalDateTime end) {
        instanceCreator.getInstance(this, group, start, end);
    }

    public static class Builder<G extends HasTitle, T extends HasTimeslot<G>, D extends TimeRowDrawable<G>> {

        HTMLCanvasElement canvas;
        Collection<T> shifts;
        Integer tenantId;
        Panel topPanel;
        Panel bottomPanel;
        Panel sidePanel;
        LocalDateTime startAt;
        Fetchable<Collection<T>> dataProvider;
        Fetchable<List<G>> groupProvider;
        DataProvider<G, T> instanceCreator;

        public Builder(HTMLCanvasElement canvas, Integer tenantId) {
            this.canvas = canvas;
            this.tenantId = tenantId;

            topPanel = null;
            bottomPanel = null;
            sidePanel = null;
            dataProvider = null;
            groupProvider = null;
            instanceCreator = null;
            startAt = null;
        }

        public Builder<G, T, D> withTopPanel(Panel topPanel) {
            this.topPanel = topPanel;
            return this;
        }

        public Builder<G, T, D> withBottomPanel(Panel bottomPanel) {
            this.bottomPanel = bottomPanel;
            return this;
        }

        public Builder<G, T, D> withSidePanel(Panel sidePanel) {
            this.sidePanel = sidePanel;
            return this;
        }

        public Builder<G, T, D> fetchingDataFrom(Fetchable<Collection<T>> dataProvider) {
            this.dataProvider = dataProvider;
            return this;
        }

        public Builder<G, T, D> fetchingGroupsFrom(Fetchable<List<G>> groupProvider) {
            this.groupProvider = groupProvider;
            return this;
        }

        public Builder<G, T, D> creatingDataInstancesWith(DataProvider<G, T> instanceCreator) {
            this.instanceCreator = instanceCreator;
            return this;
        }

        public Builder<G, T, D> startingAt(LocalDateTime start) {
            startAt = start;
            return this;
        }

        public Calendar<G, T> asTwoDayView(TimeRowDrawableProvider<G, T, D> drawableProvider) {
            if (null != topPanel && null != bottomPanel && null != sidePanel) {
                Calendar<G, T> calendar = new Calendar<>(canvas, tenantId, topPanel, bottomPanel, sidePanel,
                        dataProvider,
                        groupProvider, instanceCreator);
                TwoDayView<G, T, D> view = new TwoDayView<G, T, D>(calendar, topPanel, bottomPanel, sidePanel,
                        drawableProvider);
                calendar.setView(view);

                if (null != startAt) {
                    view.setDate(startAt);
                }
                return calendar;
            } else {
                throw new IllegalStateException("You must set all of "
                        + "(topPanel,bottomPanel,sidePanel) before calling this method.");
            }
        }

    }
}