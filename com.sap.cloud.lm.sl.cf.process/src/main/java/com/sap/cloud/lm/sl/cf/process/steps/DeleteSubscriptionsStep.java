package com.sap.cloud.lm.sl.cf.process.steps;

import java.util.List;

import javax.inject.Inject;

import org.activiti.engine.delegate.DelegateExecution;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sap.activiti.common.ExecutionStatus;
import com.sap.cloud.lm.sl.cf.core.dao.ConfigurationSubscriptionDao;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationSubscription;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationSubscription.ResourceDto;
import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.common.NotFoundException;
import com.sap.cloud.lm.sl.slp.model.StepMetadata;

@Component("deleteSubscriptionsStep")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteSubscriptionsStep extends AbstractXS2ProcessStep {

    public static StepMetadata getMetadata() {
        return StepMetadata.builder().id("deleteSubscriptionsTask").displayName("Delete Subscriptions").description(
            "Delete Subscriptions").build();
    }

    @Inject
    private ConfigurationSubscriptionDao dao;

    @Override
    protected ExecutionStatus executeStepInternal(DelegateExecution context) {
        getStepLogger().logActivitiTask();

        getStepLogger().info(Messages.DELETING_SUBSCRIPTIONS);

        List<ConfigurationSubscription> subscriptionsToDelete = StepsUtil.getSubscriptionsToDelete(context);
        for (ConfigurationSubscription subscription : subscriptionsToDelete) {
            try {
                dao.remove(subscription.getId());
            } catch (NotFoundException e) {
                ResourceDto resourceDto = subscription.getResourceDto();
                getStepLogger().warn(Messages.COULD_NOT_DELETE_SUBSCRIPTION, subscription.getAppName(), getName(resourceDto));
            }
        }

        getStepLogger().debug(Messages.DELETED_SUBSCRIPTIONS);
        return ExecutionStatus.SUCCESS;
    }

    private String getName(ResourceDto dto) {
        return dto == null ? null : dto.getName();
    }

}
