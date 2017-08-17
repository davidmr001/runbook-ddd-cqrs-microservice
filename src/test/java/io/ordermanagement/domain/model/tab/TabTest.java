package io.ordermanagement.domain.model.tab;

import io.ordermanagement.application.OpenTab;
import io.ordermanagement.application.PlaceOrder;
import io.ordermanagement.domain.model.DomainEventPublisher;
import io.ordermanagement.domain.model.tab.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TabTest {

    private static final String testTabId = UUID.randomUUID().toString();
    private static final int testTable = 3;
    private static final String testWaiter = "John";

    private static final OrderItem testDrink1 = new OrderItem(1, "beer", true, 3.0);
    private static final OrderItem testDrink2 = new OrderItem(2, "juice", true, 1.0);
    private static final OrderItem testFood1 = new OrderItem(3, "pizza", false, 8.0);
    private static final OrderItem testFood2 = new OrderItem(4, "salad", false, 7.0);

    private Tab aggregate;
    private DomainEventPublisher eventPublisherMock;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        eventPublisherMock = mock(DomainEventPublisher.class);
        aggregate = new Tab(eventPublisherMock);
    }

    @Test
    public void can_open_a_new_tab() {
        OpenTab openTab = new OpenTab(testTabId, testTable, testWaiter);

        aggregate.handle(openTab);

        verify(eventPublisherMock).publish(new TabOpened(testTabId, testTable, testWaiter));
    }

    @Test
    public void can_not_order_with_unopened_tab() {
        List<OrderItem> items = Arrays.asList(testDrink1, testDrink2);
        PlaceOrder placeOrder = new PlaceOrder(testTabId, items);

        exception.expect(TabNotOpen.class);

        aggregate.handle(placeOrder);
    }

    @Test
    public void can_place_drinks_order() {
        OpenTab openTab = new OpenTab(testTabId, testTable, testWaiter);
        List<OrderItem> drinks = Arrays.asList(testDrink1, testDrink2);
        PlaceOrder placeOrder = new PlaceOrder(testTabId, drinks);
        aggregate.handle(openTab);

        aggregate.handle(placeOrder);

        verify(eventPublisherMock).publish(new DrinksOrdered(testTabId, drinks));
    }

    @Test
    public void can_place_food_order() {
        OpenTab openTab = new OpenTab(testTabId, testTable, testWaiter);
        List<OrderItem> food = Arrays.asList(testFood1, testFood2);
        PlaceOrder placeOrder = new PlaceOrder(testTabId, food);
        aggregate.handle(openTab);

        aggregate.handle(placeOrder);

        verify(eventPublisherMock).publish(new FoodOrdered(testTabId, food));
    }

    @Test
    public void can_place_drinks_and_food_order() {
        OpenTab openTab = new OpenTab(testTabId, testTable, testWaiter);
        List<OrderItem> items = Arrays.asList(testDrink1, testFood1);
        PlaceOrder placeOrder = new PlaceOrder(testTabId, items);
        aggregate.handle(openTab);

        aggregate.handle(placeOrder);

        verify(eventPublisherMock).publish(new DrinksOrdered(testTabId, Arrays.asList(testDrink1)));
        verify(eventPublisherMock).publish(new FoodOrdered(testTabId, Arrays.asList(testFood1)));
    }

}