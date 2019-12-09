package com.spring.crud.app.example.services;

import com.spring.crud.app.example.models.Item;
import com.spring.crud.app.example.models.Order;
import com.spring.crud.app.example.models.OrderItems;
import com.spring.crud.app.example.models.TypeItem;
import com.spring.crud.app.example.repositories.OrderItemsRepository;
import com.spring.crud.app.example.repositories.OrderRepository;
import com.spring.crud.app.example.services.exceptions.BusinessException;
import com.spring.crud.app.example.services.exceptions.ResourceNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test {@link OrderService}
 *
 * @author Mauricio Generoso
 */
@RunWith(SpringRunner.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private OrderItemsRepository orderItemsRepository;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private OrderService service;

    @Test
    public void findAll_shouldCallMethodToUpdateTotalPreview() {
        // Arrange
        OrderService spyService = spy(service);
        Order order = new Order();

        Pageable pageable = PageRequest.of(0, 1);
        List<Order> orderList = Collections.singletonList(order);
        Page<Order> pageOrders = new PageImpl<>(orderList, pageable, orderList.size());

        doReturn(pageOrders).when(repository).findAll(pageable);
        doNothing().when(spyService).updateTotalPreview(order);

        // Act
        spyService.findAll(pageable);

        // Assert
        verify(repository, times(1)).findAll(pageable);
        verify(spyService, times(1)).updateTotalPreview(order);
    }

    @Test
    public void findById_shouldReturnWhenExists() {
        // Arrange
        UUID id = UUID.randomUUID();
        Order order = new Order();

        doReturn(Optional.of(order)).when(repository).findById(id);

        // Act
        Order result = service.findById(id);

        // Assert
        assertNotNull(result);
        doReturn(Optional.of(order)).when(repository).findById(id);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void findById_shouldThrowsExceptionWhenNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();

        doReturn(Optional.empty()).when(repository).findById(id);

        // Act
        service.findById(id);
    }

    @Test
    public void customFindById_shouldCallMethodToUpdateTotalPreview() {
        // Arrange
        OrderService spyService = spy(service);
        UUID id = UUID.randomUUID();

        doReturn(new Order()).when(spyService).findById(id);
        doNothing().when(spyService).updateTotalPreview(any(Order.class));

        // Act
        spyService.customFindById(id);

        // Assert
        verify(spyService, times(1)).findById(id);
        verify(spyService, times(1)).updateTotalPreview(any(Order.class));
    }

    @Test
    public void applyDiscount_shouldPassWhenOrderIsOpen() {
        // Arrange
        OrderService spyService = spy(service);

        Order order = new Order();
        int discount = 10;

        doReturn(new Order()).when(spyService).save(order);

        // Act
        spyService.applyDiscount(order, discount);

        // Assert
        verify(spyService, times(1)).save(order);
    }

    @Test(expected = BusinessException.class)
    public void applyDiscount_shouldThrowExceptionWhenOrderIsNotOpen() {
        // Arrange
        Order order = new Order();
        order.setOpen(false);
        int discount = 10;

        // Act
        service.applyDiscount(order, discount);
    }

    @Test
    public void updateTotalPreview_shouldNotApplyDiscountWhenThereIsNot() {
        // Arrange
        Order order = new Order();
        order.setDiscount(0);
        order.setTotalPreview(0);

        Item product = new Item();
        product.setType(TypeItem.PRODUCT);
        product.setPrice(100);

        Item serviceItem = new Item();
        serviceItem.setType(TypeItem.SERVICE);
        serviceItem.setPrice(100);

        OrderItems orderItemProduct = new OrderItems();
        orderItemProduct.setItem(product);
        orderItemProduct.setAmount(2);

        OrderItems orderItemService = new OrderItems();
        orderItemService.setItem(serviceItem);
        orderItemService.setAmount(2);

        order.setOrderItems(Arrays.asList(orderItemProduct, orderItemService));

        // Act
        service.updateTotalPreview(order);

        // Assert
        assertEquals("Unexpected result",400, order.getTotalPreview(), 0);
    }

    @Test
    public void updateTotalPreview_shouldApplyDiscountOnProducts() {
        // Arrange
        Order order = new Order();
        order.setDiscount(10);
        order.setTotalPreview(0);

        Item product = new Item();
        product.setType(TypeItem.PRODUCT);
        product.setPrice(100);

        Item serviceItem = new Item();
        serviceItem.setType(TypeItem.SERVICE);
        serviceItem.setPrice(100);

        OrderItems orderItemProduct = new OrderItems();
        orderItemProduct.setItem(product);
        orderItemProduct.setAmount(2);

        OrderItems orderItemService = new OrderItems();
        orderItemService.setItem(serviceItem);
        orderItemService.setAmount(2);

        order.setOrderItems(Arrays.asList(orderItemProduct, orderItemService));

        // Act
        service.updateTotalPreview(order);

        // Assert
        assertEquals("Unexpected result",380, order.getTotalPreview(), 0);
    }
}