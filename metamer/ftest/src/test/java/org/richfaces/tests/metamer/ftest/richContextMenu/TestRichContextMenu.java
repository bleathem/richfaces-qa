/**
 * JBoss, Home of Professional Open Source
 * Copyright 2010-2013, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.richfaces.tests.metamer.ftest.richContextMenu;

import static org.jboss.arquillian.ajocado.utils.URLUtils.buildUrl;
import static org.jboss.arquillian.graphene.Graphene.guardHttp;
import static org.jboss.arquillian.graphene.Graphene.guardNoRequest;
import static org.jboss.arquillian.graphene.Graphene.guardXhr;
import static org.richfaces.tests.metamer.ftest.webdriver.AttributeList.contextMenuAttributes;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.net.URL;

import org.jboss.arquillian.graphene.spi.annotations.Page;
import org.openqa.selenium.HasInputDevices;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Mouse;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.Locatable;
import org.richfaces.component.Positioning;
import org.richfaces.tests.metamer.ftest.AbstractWebDriverTest;
import org.richfaces.tests.metamer.ftest.annotations.Inject;
import org.richfaces.tests.metamer.ftest.annotations.IssueTracking;
import org.richfaces.tests.metamer.ftest.annotations.Use;
import org.richfaces.tests.page.fragments.impl.contextMenu.RichFacesContextMenu;
import org.testng.annotations.Test;

/**
 * Test for rich:contextMenu component at faces/components/richContextMenu/simple.xhtml
 *
 * @author <a href="mailto:jpapouse@redhat.com">Jan Papousek</a>
 * @author <a href="mailto:jstefek@redhat.com">Jiri Stefek</a>
 * @author <a href="mailto:jhuska@redhat.com">Juraj Huska</a>
 * @since 4.2.1.Final
 */
public class TestRichContextMenu extends AbstractWebDriverTest {

    @Page
    private ContextMenuSimplePage page;
    @Inject
    @Use(empty = false)
    private Integer delay;

    @Override
    public URL getTestUrl() {
        return buildUrl(contextPath, "faces/components/richContextMenu/simple.xhtml");
    }

    private void updateShowAction() {
        contextMenuAttributes.set(ContextMenuAttributes.showEvent, "click");
        page.contextMenu.setInvoker(RichFacesContextMenu.LEFT_CLICK);
    }

    @Test
    @Use(field = "delay", ints = { 1000, 1500, 2500 })
    public void testHideDelay() {
        updateShowAction();
        double differenceThreshold = delay * 0.5;
        // set hideDelay
        contextMenuAttributes.set(ContextMenuAttributes.hideDelay, delay);
        // show context menu
        page.clickOnFirstPanel(driverType);
        // check whether the context menu is displayed
        page.waitUntilContextMenuAppears();
        // save the time
        double beforeTime = System.currentTimeMillis();
        // blur >>> menu will disappear after delay
        page.clickOnSecondPanel(driverType);
        assertTrue(page.contextMenuContent.isDisplayed());
        // wait until menu hides
        page.waitUntilContextMenuHides();

        double diff = System.currentTimeMillis() - beforeTime;
        double diff2 = Math.abs(diff - delay);
        assertTrue(diff2 < differenceThreshold, "The measured delay was far" + " from set value. The difference was: " + diff2
                + ". The difference threshold was: " + differenceThreshold);
    }

    @Test
    public void testOnhide() {
        updateShowAction();
        final String VALUE = "hide";
        // set onhide
        contextMenuAttributes.set(ContextMenuAttributes.onhide, "metamerEvents += \"" + VALUE + "\"");
        // show context menu
        page.clickOnFirstPanel(driverType);
        // check whether the context menu is displayed
        page.waitUntilContextMenuAppears();
        // lose focus >>> menu will disappear
        page.clickOnSecondPanel(driverType);
        // check whether the context menu isn't displayed
        page.waitUntilContextMenuHides();
        assertEquals(expectedReturnJS("return metamerEvents", VALUE), VALUE);
    }

    @Test
    public void testVerticalOffset() {
        updateShowAction();
        int offset = 11;
        // show context menu
        page.clickOnFirstPanel(driverType);
        // check whether the context menu is displayed
        page.waitUntilContextMenuAppears();
        // get position before offset is set
        Point before = page.contextMenuContent.getLocation();
        // set verticalOffset
        contextMenuAttributes.set(ContextMenuAttributes.verticalOffset, offset);
        // show context menu
        page.clickOnFirstPanel(driverType);
        // check whether the context menu is displayed
        page.waitUntilContextMenuAppears();
        // get position after offset is set
        Point after = page.contextMenuContent.getLocation();
        // check offset
        assertEquals(before.getY(), after.getY() - offset);
    }

    @Test
    public void testDir() {
        updateShowAction();
        String expected = "rtl";
        contextMenuAttributes.set(ContextMenuAttributes.dir, expected);

        page.contextMenu.invoke(page.targetPanel1);
        String directionCSS = page.contextMenu.getItems().get(0).getCssValue("direction");
        assertEquals(directionCSS, expected, "The direction attribute was not applied correctly!");
    }

    @Test
    public void testLang() {
        updateShowAction();
        String langVal = "cs";
        contextMenuAttributes.set(ContextMenuAttributes.lang, langVal);
        page.contextMenu.invoke(page.targetPanel1);

        assertEquals(page.contextMenu.getLangAttribute(), langVal, "The lang attribute was not set correctly!");
    }

    @Test
    public void testMode() {
        updateShowAction();
        // ajax
        contextMenuAttributes.set(ContextMenuAttributes.mode, "ajax");
        page.contextMenu.invoke(page.targetPanel1);
        guardXhr(page.contextMenu.getItems().get(0)).click();
        assertEquals(page.output.getText(), "Open", "Menu action was not performed.");

        // server
        contextMenuAttributes.set(ContextMenuAttributes.mode, "server");
        page.contextMenu.invoke(page.targetPanel1);
        guardHttp(page.contextMenu.getItems().get(8)).click();
        assertEquals(page.output.getText(), "Exit", "Menu action was not performed.");

        // client
        contextMenuAttributes.set(ContextMenuAttributes.mode, "client");
        page.contextMenu.invoke(page.targetPanel1);
        guardNoRequest(page.contextMenu.getItems().get(0)).click();
    }

    @Test
    public void testDisabled() {
        updateShowAction();
        page.contextMenu.invoke(page.targetPanel1);
        assertTrue(page.contextMenuContent.isDisplayed());

        contextMenuAttributes.set(ContextMenuAttributes.disabled, true);

        try {
            page.contextMenu.invoke(page.targetPanel1);
            fail("The context menu should not be invoked when disabled!");
        } catch (TimeoutException ex) {
            //OK
        }
    }

    @Test
    public void testHorizontalOffset() {
        updateShowAction();
        int offset = 11;

        page.contextMenu.invoke(page.targetPanel1);
        Point positionBefore = page.contextMenuContent.getLocation();

        contextMenuAttributes.set(ContextMenuAttributes.horizontalOffset, offset);
        page.contextMenu.invoke(page.targetPanel1);

        Point positionAfter = page.contextMenuContent.getLocation();

        assertEquals(positionAfter.getX(), positionBefore.getX() + offset);
    }

    @Test
    public void testStyle() {
        updateShowAction();
        String color = "yellow";
        String styleVal = "background-color: " + color + ";";
        contextMenuAttributes.set(ContextMenuAttributes.style, styleVal);
        page.contextMenu.invoke(page.targetPanel1);
        String backgroundColor = page.contextMenuRoot.getCssValue("background-color");
        // webdriver retrieves the color in rgba format
        assertEquals(page.trimTheRGBAColor(backgroundColor), "rgba(255,255,0,1)", "The style was not applied correctly!");
    }

    @Test
    public void testStyleClass() {
        updateShowAction();
        String styleClassVal = "test-style-class";
        contextMenuAttributes.set(ContextMenuAttributes.styleClass, styleClassVal);
        String styleClass = page.contextMenuRoot.getAttribute("class");
        assertTrue(styleClass.contains(styleClassVal));
    }

    @Test
    public void testTarget() {
        updateShowAction();
        // contextMenu element is present always. Check if is displayed
        contextMenuAttributes.set(ContextMenuAttributes.target, "targetPanel2");
        assertFalse(page.contextMenuContent.isDisplayed());
        page.contextMenu.invoke(page.targetPanel2);

        contextMenuAttributes.set(ContextMenuAttributes.target, "targetPanel1");
        assertFalse(page.contextMenuContent.isDisplayed());
        page.contextMenu.invoke(page.targetPanel1);
    }

    @Test
    public void testTitle() {
        updateShowAction();
        String titleVal = "test title";
        contextMenuAttributes.set(ContextMenuAttributes.title, titleVal);
        assertEquals(page.contextMenuRoot.getAttribute("title"), titleVal);
    }

    @Test
    public void testRendered() {
        updateShowAction();
        contextMenuAttributes.set(ContextMenuAttributes.rendered, Boolean.FALSE);
        try {
            page.contextMenu.invoke(page.targetPanel1);
            fail("The context menu should not be invoked when rendered == false!");
        } catch (TimeoutException ex) {
            //OK
        }
    }

    @Test
    public void testPopupWidth() {
        updateShowAction();
        String minWidth = "333";
        contextMenuAttributes.set(ContextMenuAttributes.popupWidth, minWidth);
        page.contextMenu.invoke(page.targetPanel1);
        String style = page.contextMenuContent.getCssValue("min-width");
        assertEquals(style, minWidth + "px");
    }

    @Test
    public void testOnshow() {
        updateShowAction();
        testFireEvent(contextMenuAttributes, ContextMenuAttributes.onshow, new Actions(driver).click(page.targetPanel1).build());
    }

    @Test
    public void testOnclick() {
        updateShowAction();
        testFireEvent(contextMenuAttributes, ContextMenuAttributes.onclick, new Action() {
            @Override
            public void perform() {
                page.contextMenu.invoke(page.targetPanel1);
                page.contextMenu.getItems().get(1).click();
            }
        });
    }

    @Test
    public void testOndblclick() {
        updateShowAction();
        testFireEvent(contextMenuAttributes, ContextMenuAttributes.ondblclick, new Action() {
            @Override
            public void perform() {
                page.contextMenu.invoke(page.targetPanel1);
                new Actions(driver)
                .doubleClick(page.contextMenu.getItems().get(1))
                .build().perform();
            }
        });
    }

    @Test
    public void testOnitemclick() {
        updateShowAction();
        testOnclick();
    }

    @Test(groups = "4.Future")
    @IssueTracking("https://issues.jboss.org/browse/RF-12792")
    public void testOnkeydown() {
        updateShowAction();
        testFireEvent(contextMenuAttributes, ContextMenuAttributes.onkeydown, new Action() {
            @Override
            public void perform() {
                page.contextMenu.invoke(page.targetPanel1);
                new Actions(driver)
                .keyDown(page.contextMenu.getItems().get(1), Keys.CONTROL)
                .keyUp(page.contextMenu.getItems().get(1), Keys.CONTROL)
                .build().perform();
            }
        });
    }

    @Test(groups = "4.Future")
    //false negative
    public void testOnkeyup() {
        updateShowAction();
        testFireEvent(contextMenuAttributes, ContextMenuAttributes.onkeyup, new Action() {
            @Override
            public void perform() {
                page.contextMenu.invoke(page.targetPanel1);
                new Actions(driver)
                .keyDown(page.contextMenu.getItems().get(0), Keys.ALT)
                .keyUp(page.contextMenu.getItems().get(0), Keys.ALT)
                .build().perform();
            }
        });
    }

    @Test
    public void testOnkeypress() {
        updateShowAction();
        testFireEvent(contextMenuAttributes, ContextMenuAttributes.onkeypress, new Action() {
            @Override
            public void perform() {
                page.contextMenu.invoke(page.targetPanel1);
                new Actions(driver)
                .sendKeys("a")
                .build().perform();
            }
        });
    }

    @Test
    public void testOnmousedown() {
        updateShowAction();
        testFireEvent(contextMenuAttributes, ContextMenuAttributes.onmousedown, new Action() {
            @Override
            public void perform() {
                page.contextMenu.invoke(page.targetPanel1);
                Mouse mouse = ((HasInputDevices) driver).getMouse();
                mouse.mouseDown(((Locatable) page.contextMenu.getItems().get(1)).getCoordinates());
            }
        });
    }

    @Test
    public void testOnmouseup() {
        updateShowAction();
        testFireEvent(contextMenuAttributes, ContextMenuAttributes.onmouseup, new Action() {
            @Override
            public void perform() {
                page.contextMenu.invoke(page.targetPanel1);
                Mouse mouse = ((HasInputDevices) driver).getMouse();
                mouse.mouseUp(((Locatable) page.contextMenu.getItems().get(1)).getCoordinates());
            }
        });
    }

    @Test
    public void testOnmousemove() {
        updateShowAction();
        testFireEvent(contextMenuAttributes, ContextMenuAttributes.onmousemove, new Action() {
            @Override
            public void perform() {
                page.contextMenu.invoke(page.targetPanel1);
                new Actions(driver)
                .moveToElement(page.contextMenu.getItems().get(1))
                .build().perform();
            }
        });
    }

    @Test
    public void testOnmouseout() {
        updateShowAction();
        testFireEvent(contextMenuAttributes, ContextMenuAttributes.onmousemove, new Action() {
            @Override
            public void perform() {
                page.contextMenu.invoke(page.targetPanel1);
                new Actions(driver)
                .moveToElement(page.contextMenu.getItems().get(1))
                .moveToElement(page.body)
                .build().perform();
            }
        });
    }

    @Test
    public void testOnmouseover() {
        updateShowAction();
        testOnmousemove();
    }

    @Test
    public void testOngroupshow() {
        updateShowAction();
        testFireEvent(contextMenuAttributes, ContextMenuAttributes.onmousemove, new Action() {
            @Override
            public void perform() {
                page.contextMenu.invoke(page.targetPanel1);
                new Actions(driver).moveToElement(page.contextMenu.getItems().get(2)).build().perform();
            }
        });
    }

    @Test
    public void testOngrouphide() {
        updateShowAction();
        testFireEvent(contextMenuAttributes, ContextMenuAttributes.onmousemove, new Action() {
            @Override
            public void perform() {
                page.contextMenu.invoke(page.targetPanel1);
                new Actions(driver)
                .moveToElement(page.contextMenu.getItems().get(2))
                .moveToElement(page.body)
                .build().perform();
            }
        });
    }

    @Test
    public void testDirection() {
        updateShowAction();
        //setting up the right panel cause then the context menu fit on the page
        contextMenuAttributes.set(ContextMenuAttributes.target, "targetPanel2");

        Point contextMenuLocation = page.getContextMenuLocationWhenPosition(Positioning.topLeft);
        assertEquals(contextMenuLocation, page.getExpectedLocation(Positioning.topLeft));

        contextMenuLocation = page.getContextMenuLocationWhenPosition(Positioning.topRight);
        assertEquals(contextMenuLocation, page.getExpectedLocation(Positioning.topRight));

        contextMenuLocation = page.getContextMenuLocationWhenPosition(Positioning.bottomAuto);
        assertEquals(contextMenuLocation, page.getExpectedLocation(Positioning.bottomRight));

        contextMenuLocation = page.getContextMenuLocationWhenPosition(Positioning.bottomLeft);
        assertEquals(contextMenuLocation, page.getExpectedLocation(Positioning.bottomLeft));

        contextMenuLocation = page.getContextMenuLocationWhenPosition(Positioning.bottomRight);
        assertEquals(contextMenuLocation, page.getExpectedLocation(Positioning.bottomRight));

        contextMenuLocation = page.getContextMenuLocationWhenPosition(Positioning.autoLeft);
        assertEquals(contextMenuLocation, page.getExpectedLocation(Positioning.bottomLeft));

        contextMenuLocation = page.getContextMenuLocationWhenPosition(Positioning.autoRight);
        assertEquals(contextMenuLocation, page.getExpectedLocation(Positioning.bottomRight));

        contextMenuLocation = page.getContextMenuLocationWhenPosition(Positioning.topAuto);
        assertEquals(contextMenuLocation, page.getExpectedLocation(Positioning.topRight));

        contextMenuLocation = page.getContextMenuLocationWhenPosition(Positioning.auto);
        assertEquals(contextMenuLocation, page.getExpectedLocation(Positioning.bottomRight));
    }

    @Test
    public void testShowDelay() {
        updateShowAction();
        page.checkShowDelay(2000);
        page.checkShowDelay(1000);
        page.checkShowDelay(500);
    }

    @Test
    public void testShowEvent() {
        updateShowAction();
        contextMenuAttributes.set(ContextMenuAttributes.showEvent, "hover");
        page.contextMenu.setInvoker(RichFacesContextMenu.HOVER);
        page.contextMenu.invoke(page.targetPanel1);

        contextMenuAttributes.set(ContextMenuAttributes.showEvent, "click");
        page.contextMenu.setInvoker(RichFacesContextMenu.LEFT_CLICK);
        page.contextMenu.invoke(page.targetPanel1);
    }

    @Test
    public void testTargetSelector() {
        updateShowAction();
        contextMenuAttributes.set(ContextMenuAttributes.targetSelector, "div[id*=targetPanel]");

        page.contextMenu.invoke(page.targetPanel1);
        page.contextMenu.dismiss();
    }
}
