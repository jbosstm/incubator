/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package io.narayana.lra.checker;

import io.narayana.lra.checker.bean.AsyncSuspendWithoutForgetBean;
import io.narayana.lra.checker.bean.CorrectBean;
import io.narayana.lra.checker.bean.CorrectMethodLRABean;
import io.narayana.lra.checker.bean.NoPutOrGetBean;
import io.narayana.lra.checker.bean.hierarchy.base.ChildLRABean;
import io.narayana.lra.checker.bean.hierarchy.wrong.BadParent;
import io.narayana.lra.checker.bean.hierarchy.wrong.GoodLRAChild;
import io.narayana.lra.checker.bean.jaxrs.AfterLRAWithoutPutBean;
import io.narayana.lra.checker.bean.jaxrs.CompensateWithoutPutBean;
import io.narayana.lra.checker.bean.jaxrs.CompleteWithoutPutBean;
import io.narayana.lra.checker.bean.jaxrs.ForgetWithoutDeleteBean;
import io.narayana.lra.checker.bean.jaxrs.LeaveWithoutPutBean;
import io.narayana.lra.checker.bean.jaxrs.StatusWithoutGetBean;
import io.narayana.lra.checker.bean.lra.NoLRAAbstractClass;
import io.narayana.lra.checker.bean.lra.NoLRAClass;
import io.narayana.lra.checker.bean.lra.NoLRAEnum;
import io.narayana.lra.checker.bean.lra.NoLRAInterface;
import io.narayana.lra.checker.bean.multi.MultiAfterLRABean;
import io.narayana.lra.checker.bean.multi.MultiCompensateBean;
import io.narayana.lra.checker.bean.multi.MultiCompleteBean;
import io.narayana.lra.checker.bean.multi.MultiForgetBean;
import io.narayana.lra.checker.bean.multi.MultiLeaveBean;
import io.narayana.lra.checker.bean.multi.MultiStatusBean;
import io.narayana.lra.checker.bean.multionmethod.CompleteAndCompensateBean;
import io.narayana.lra.checker.bean.multionmethod.CompleteAndStatusBean;
import io.narayana.lra.checker.bean.multionmethod.LeaveAndForgetAndAfterLRABean;
import io.narayana.lra.checker.bean.nonjaxrs.AfterLRANonJaxrsBean;
import io.narayana.lra.checker.bean.nonjaxrs.AfterLRANonJaxrsFailureBean;
import io.narayana.lra.checker.bean.nonjaxrs.CompensateNonJaxrsBean;
import io.narayana.lra.checker.bean.nonjaxrs.CompensateNonJaxrsFailureBean;
import io.narayana.lra.checker.bean.nonjaxrs.CompleteNonJaxrsBean;
import io.narayana.lra.checker.bean.nonjaxrs.CompleteNonJaxrsFailureBean;
import io.narayana.lra.checker.bean.nonjaxrs.ForgetNonJaxrsBean;
import io.narayana.lra.checker.bean.nonjaxrs.ForgetNonJaxrsFailureBean;
import io.narayana.lra.checker.bean.nonjaxrs.StatusNonJaxrsBean;
import io.narayana.lra.checker.bean.nonjaxrs.StatusNonJaxrsFailureBean;
import io.narayana.lra.checker.failures.FailureCatalog;
import org.eclipse.microprofile.lra.annotation.AfterLRA;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.Forget;
import org.eclipse.microprofile.lra.annotation.Status;
import org.eclipse.microprofile.lra.annotation.ws.rs.Leave;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;

import static io.narayana.lra.checker.common.TestProcessor.assertFailureCatalogContains;
import static io.narayana.lra.checker.common.TestProcessor.initWeld;
import static io.narayana.lra.checker.failures.ErrorCode.MISSING_SUSPEND_ASYNC_CALLBACK;
import static io.narayana.lra.checker.failures.ErrorCode.MULTIPLE_ANNOTATIONS_OF_THE_SAME_TYPE;
import static io.narayana.lra.checker.failures.ErrorCode.MULTIPLE_ANNOTATIONS_OF_VARIOUS_TYPES;
import static io.narayana.lra.checker.failures.ErrorCode.NO_LRA;
import static io.narayana.lra.checker.failures.ErrorCode.WRONG_JAXRS_COMPLEMENTARY_ANNOTATION;
import static io.narayana.lra.checker.failures.ErrorCode.WRONG_METHOD_SIGNATURE_NON_JAXRS_RESOURCE;

/**
 * Test case which checks functionality of CDI extension by deploying wrongly
 * composed LRA components and expect an deployment exception to be thrown.
 */
public class LraCdiCheckTest {

    @Before
    public void cleanUp() {
        FailureCatalog.INSTANCE.clear();
    }

    // ------------------- LRA annotations
    // ----------------------------------------------------
    @Test
    public void lraIsMissingOnClass() {
        initWeld(NoLRAClass.class);
        assertFailureCatalogContains(NoLRAClass.class, NO_LRA.toString());
    }

    @Test
    public void lraIsMissingOnAbstractInterface() {
        initWeld(NoLRAAbstractClass.class);
        initWeld(NoLRAInterface.class);
        initWeld(NoLRAEnum.class);
        Assert.assertTrue("No failure expected for " + NoLRAAbstractClass.class.getName() + " and " + NoLRAInterface.class.getName() +
                " and " + NoLRAEnum.class.getName() +
                " but was " + FailureCatalog.INSTANCE.formatCatalogContent(), FailureCatalog.INSTANCE.isEmpty());
    }

    // ------------------- JAX-RS complementary annotations
    // ----------------------------------------------------
    @Test
    public void methodTypeAnnotationMissing() {
        initWeld(NoPutOrGetBean.class);
        assertFailureCatalogContains(NoPutOrGetBean.class, WRONG_JAXRS_COMPLEMENTARY_ANNOTATION, GET.class);
        assertFailureCatalogContains(NoPutOrGetBean.class, WRONG_JAXRS_COMPLEMENTARY_ANNOTATION, PUT.class);
    }

    @Test
    public void forgetMissingDelete() {
        initWeld(ForgetWithoutDeleteBean.class);
        assertFailureCatalogContains(ForgetWithoutDeleteBean.class, WRONG_JAXRS_COMPLEMENTARY_ANNOTATION, DELETE.class);
    }

    @Test
    public void leaveMissingPut() {
        initWeld(LeaveWithoutPutBean.class);
        assertFailureCatalogContains(LeaveWithoutPutBean.class, WRONG_JAXRS_COMPLEMENTARY_ANNOTATION, PUT.class);
    }

    @Test
    public void completeMissingPut() {
        initWeld(CompleteWithoutPutBean.class);
        assertFailureCatalogContains(CompleteWithoutPutBean.class, WRONG_JAXRS_COMPLEMENTARY_ANNOTATION, PUT.class);
    }

    @Test
    public void compensateMissingPut() {
        initWeld(CompensateWithoutPutBean.class);
        assertFailureCatalogContains(CompensateWithoutPutBean.class, WRONG_JAXRS_COMPLEMENTARY_ANNOTATION, PUT.class);
    }

    @Test
    public void afterLRAMissingPut() {
        initWeld(AfterLRAWithoutPutBean.class);
        assertFailureCatalogContains(AfterLRAWithoutPutBean.class, WRONG_JAXRS_COMPLEMENTARY_ANNOTATION, PUT.class);
    }

    @Test
    public void statusMissingPut() {
        initWeld(StatusWithoutGetBean.class);
        assertFailureCatalogContains(StatusWithoutGetBean.class, WRONG_JAXRS_COMPLEMENTARY_ANNOTATION, GET.class);
    }

    // ------------------- JAX-RS async
    // ----------------------------------------------------
    @Test
    public void asyncInvocationWithoutForgetDefined() {
        initWeld(AsyncSuspendWithoutForgetBean.class);
        assertFailureCatalogContains(AsyncSuspendWithoutForgetBean.class, MISSING_SUSPEND_ASYNC_CALLBACK, Complete.class);
    }

    // ------------------- Correct
    // ----------------------------------------------------
    @Test
    public void allCorrect() {
        initWeld(CorrectBean.class);
        Assert.assertTrue("No failure expected at " + CorrectBean.class.getName() + " but was "
            + FailureCatalog.INSTANCE.formatCatalogContent(), FailureCatalog.INSTANCE.isEmpty());
    }

    @Test
    public void allCorrectLRAOnMethod() {
        initWeld(CorrectMethodLRABean.class);
        Assert.assertTrue("No failure expected at " + CorrectMethodLRABean.class.getName() + " but was "
                + FailureCatalog.INSTANCE.formatCatalogContent(), FailureCatalog.INSTANCE.isEmpty());
    }

    @Test
    public void noLraContext() {
        initWeld(CorrectMethodLRABean.class);
        Assert.assertTrue("No failure expected at " + CorrectMethodLRABean.class.getName() + " but was "
                + FailureCatalog.INSTANCE.formatCatalogContent(), FailureCatalog.INSTANCE.isEmpty());
    }

    @Test
    public void hierarchyAfterLRA() {
        initWeld(ChildLRABean.class);
        Assert.assertTrue("No failure expected at " + ChildLRABean.class.getName() + " but was "
                + FailureCatalog.INSTANCE.formatCatalogContent(), FailureCatalog.INSTANCE.isEmpty());
    }

    @Test
    public void hierarchyGoodChildBadParent() {
        initWeld(BadParent.class);
        initWeld(GoodLRAChild.class);
        Assert.assertTrue("No failure expected at " + GoodLRAChild.class.getName() + " but was "
                + FailureCatalog.INSTANCE.formatCatalogContent(), FailureCatalog.INSTANCE.isEmpty());
    }

    // ------------------- Multiple annotations of the same type
    // ---------------------------------------------------------
    @Test
    public void multiForgetAnnotation() {
        initWeld(MultiForgetBean.class);
        assertFailureCatalogContains(MultiForgetBean.class, MULTIPLE_ANNOTATIONS_OF_THE_SAME_TYPE, Forget.class);
    }

    @Test
    public void multiCompleteAnnotation() {
        initWeld(MultiCompleteBean.class);
        assertFailureCatalogContains(MultiCompleteBean.class, MULTIPLE_ANNOTATIONS_OF_THE_SAME_TYPE, Complete.class);
    }

    @Test
    public void multiCompensateAnnotation() {
        initWeld(MultiCompensateBean.class);
        assertFailureCatalogContains(MultiCompensateBean.class, MULTIPLE_ANNOTATIONS_OF_THE_SAME_TYPE, Compensate.class);
    }

    @Test
    public void multiAfterLRAAnnotation() {
        initWeld(MultiAfterLRABean.class);
        assertFailureCatalogContains(MultiAfterLRABean.class, MULTIPLE_ANNOTATIONS_OF_THE_SAME_TYPE, AfterLRA.class);
    }

    @Test
    public void multiLeaveAnnotation() {
        initWeld(MultiLeaveBean.class);
        assertFailureCatalogContains(MultiLeaveBean.class, MULTIPLE_ANNOTATIONS_OF_THE_SAME_TYPE, Leave.class);
    }

    @Test
    public void multiStatusAnnotation() {
        initWeld(MultiStatusBean.class);
        assertFailureCatalogContains(MultiStatusBean.class, MULTIPLE_ANNOTATIONS_OF_THE_SAME_TYPE, Status.class);
    }

    // ------------------- non JAX-RS/CDI
    // ----------------------------------------------------
    @Test
    public void compensateNonJaxRS() {
        initWeld(CompensateNonJaxrsBean.class);
        Assert.assertTrue("No failure expected at " + CompensateNonJaxrsBean.class.getName() + " but was "
                + FailureCatalog.INSTANCE.formatCatalogContent(), FailureCatalog.INSTANCE.isEmpty());
    }

    @Test
    public void compensateNonJaxRSFailure() {
        initWeld(CompensateNonJaxrsFailureBean.class);
        assertFailureCatalogContains(CompensateNonJaxrsFailureBean.class, WRONG_METHOD_SIGNATURE_NON_JAXRS_RESOURCE, Compensate.class);
    }

    @Test
    public void completeNonJaxRS() {
        initWeld(CompleteNonJaxrsBean.class);
        Assert.assertTrue("No failure expected at " + CompleteNonJaxrsBean.class.getName() + " but was "
                + FailureCatalog.INSTANCE.formatCatalogContent(), FailureCatalog.INSTANCE.isEmpty());
    }

    @Test
    public void completeNonJaxRSFailure() {
        initWeld(CompleteNonJaxrsFailureBean.class);
        assertFailureCatalogContains(CompleteNonJaxrsFailureBean.class, WRONG_METHOD_SIGNATURE_NON_JAXRS_RESOURCE, Complete.class);
    }

    @Test
    public void statusNonJaxRS() {
        initWeld(StatusNonJaxrsBean.class);
        Assert.assertTrue("No failure expected at " + StatusNonJaxrsBean.class.getName() + " but was "
                + FailureCatalog.INSTANCE.formatCatalogContent(), FailureCatalog.INSTANCE.isEmpty());
    }

    @Test
    public void statusNonJaxRSFailure() {
        initWeld(StatusNonJaxrsFailureBean.class);
        assertFailureCatalogContains(StatusNonJaxrsFailureBean.class, WRONG_METHOD_SIGNATURE_NON_JAXRS_RESOURCE, Status.class);
    }

    @Test
    public void forgetNonJaxRS() {
        initWeld(ForgetNonJaxrsBean.class);
        Assert.assertTrue("No failure expected at " + ForgetNonJaxrsBean.class.getName() + " but was "
                + FailureCatalog.INSTANCE.formatCatalogContent(), FailureCatalog.INSTANCE.isEmpty());
    }

    @Test
    public void forgetNonJaxRSFailure() {
        initWeld(ForgetNonJaxrsFailureBean.class);
        assertFailureCatalogContains(ForgetNonJaxrsFailureBean.class, WRONG_METHOD_SIGNATURE_NON_JAXRS_RESOURCE, Forget.class);
    }

    @Test
    public void afterLraNonJaxRS() {
        initWeld(AfterLRANonJaxrsBean.class);
        Assert.assertTrue("No failure expected at " + AfterLRANonJaxrsBean.class.getName() + " but was "
                + FailureCatalog.INSTANCE.formatCatalogContent(), FailureCatalog.INSTANCE.isEmpty());
    }

    @Test
    public void afterLraNonJaxRSFailure() {
        initWeld(AfterLRANonJaxrsFailureBean.class);
        assertFailureCatalogContains(AfterLRANonJaxrsFailureBean.class, WRONG_METHOD_SIGNATURE_NON_JAXRS_RESOURCE, AfterLRA.class);
    }

    // ------------------- multiple annotations of different type on method
    // --------------------------------------------------------------------
    @Test
    public void multiOnMethodCompleteCompensate() {
        initWeld(CompleteAndCompensateBean.class);
        assertFailureCatalogContains(CompleteAndCompensateBean.class, MULTIPLE_ANNOTATIONS_OF_VARIOUS_TYPES, Compensate.class);
        assertFailureCatalogContains(CompleteAndCompensateBean.class, MULTIPLE_ANNOTATIONS_OF_VARIOUS_TYPES, Complete.class);
    }

    @Test
    public void multiOnMethodCompleteStatus() {
        initWeld(CompleteAndStatusBean.class);
        assertFailureCatalogContains(CompleteAndStatusBean.class, MULTIPLE_ANNOTATIONS_OF_VARIOUS_TYPES, Status.class);
        assertFailureCatalogContains(CompleteAndStatusBean.class, MULTIPLE_ANNOTATIONS_OF_VARIOUS_TYPES, Complete.class);
        assertFailureCatalogContains(CompleteAndStatusBean.class, WRONG_JAXRS_COMPLEMENTARY_ANNOTATION, GET.class);
    }

    @Test
    public void multiOnMethodLeaveForget() {
        initWeld(LeaveAndForgetAndAfterLRABean.class);
        assertFailureCatalogContains(LeaveAndForgetAndAfterLRABean.class, MULTIPLE_ANNOTATIONS_OF_VARIOUS_TYPES, Leave.class);
        assertFailureCatalogContains(LeaveAndForgetAndAfterLRABean.class, MULTIPLE_ANNOTATIONS_OF_VARIOUS_TYPES, Forget.class);
        assertFailureCatalogContains(LeaveAndForgetAndAfterLRABean.class, MULTIPLE_ANNOTATIONS_OF_VARIOUS_TYPES, Compensate.class);
        assertFailureCatalogContains(LeaveAndForgetAndAfterLRABean.class, MULTIPLE_ANNOTATIONS_OF_VARIOUS_TYPES, AfterLRA.class);
    }
}
