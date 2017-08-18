package com.sap.cloud.lm.sl.cf.process.steps;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.cloudfoundry.client.lib.CloudFoundryException;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sap.activiti.common.ExecutionStatus;
import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.common.SLException;
import com.sap.cloud.lm.sl.slp.model.StepMetadata;

@Component("addDomainsStep")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class AddDomainsStep extends AbstractXS2ProcessStep {

    public static StepMetadata getMetadata() {
        return StepMetadata.builder().id("addDomainsTask").displayName("Add Domains").description("Add Domains").build();
    }

    @Override
    protected ExecutionStatus executeStepInternal(DelegateExecution context) throws SLException {
        getStepLogger().logActivitiTask();
        try {
            getStepLogger().info(Messages.ADDING_DOMAINS);

            CloudFoundryOperations client = getCloudFoundryClient(context);

            List<CloudDomain> existingDomains = client.getDomainsForOrg();
            List<String> existingDomainNames = getDomainNames(existingDomains);
            getStepLogger().debug("Existing domains: " + existingDomainNames);

            List<String> customDomains = StepsUtil.getCustomDomains(context);
            addDomains(client, customDomains, existingDomainNames);

            getStepLogger().debug(Messages.DOMAINS_ADDED);
            return ExecutionStatus.SUCCESS;
        } catch (CloudFoundryException cfe) {
            SLException e = StepsUtil.createException(cfe);
            getStepLogger().error(e, Messages.ERROR_ADDING_DOMAINS);
            throw e;
        } catch (SLException e) {
            getStepLogger().error(e, Messages.ERROR_ADDING_DOMAINS);
            throw e;
        }
    }

    private List<String> getDomainNames(List<CloudDomain> domains) {
        List<String> domainNames = new ArrayList<>();
        for (CloudDomain domain : domains) {
            domainNames.add(domain.getName());
        }
        return domainNames;
    }

    private void addDomains(CloudFoundryOperations client, List<String> domainNames, List<String> existingDomainNames) {
        for (String domainName : domainNames) {
            addDomain(client, domainName, existingDomainNames);
        }
    }

    private void addDomain(CloudFoundryOperations client, String domainName, List<String> existingDomainNames) {
        if (existingDomainNames.contains(domainName)) {
            getStepLogger().debug(Messages.DOMAIN_ALREADY_EXISTS, domainName);
        } else {
            getStepLogger().info(Messages.ADDING_DOMAIN, domainName);
            client.addDomain(domainName);
        }
    }

}
