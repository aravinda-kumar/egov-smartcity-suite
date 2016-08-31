/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.works.abstractestimate.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.egov.assets.model.Asset;
import org.egov.assets.service.AssetService;
import org.egov.commons.CFinancialYear;
import org.egov.commons.dao.EgwStatusHibernateDAO;
import org.egov.commons.dao.EgwTypeOfWorkHibernateDAO;
import org.egov.commons.dao.FinancialYearDAO;
import org.egov.commons.dao.FunctionHibernateDAO;
import org.egov.commons.dao.FundHibernateDAO;
import org.egov.commons.service.UOMService;
import org.egov.dao.budget.BudgetGroupDAO;
import org.egov.eis.entity.Assignment;
import org.egov.eis.service.AssignmentService;
import org.egov.eis.service.PositionMasterService;
import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.egov.infra.admin.master.service.BoundaryService;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.utils.autonumber.AutonumberServiceBeanResolver;
import org.egov.infra.validation.exception.ValidationException;
import org.egov.infra.workflow.matrix.entity.WorkFlowMatrix;
import org.egov.infra.workflow.service.SimpleWorkflowService;
import org.egov.pims.commons.Position;
import org.egov.services.masters.SchemeService;
import org.egov.works.abstractestimate.entity.AbstractEstimate;
import org.egov.works.abstractestimate.entity.AbstractEstimate.EstimateStatus;
import org.egov.works.abstractestimate.entity.AbstractEstimate.OfflineStatusesForAbstractEstimate;
import org.egov.works.abstractestimate.entity.AbstractEstimateForLoaSearchRequest;
import org.egov.works.abstractestimate.entity.AbstractEstimateForLoaSearchResult;
import org.egov.works.abstractestimate.entity.Activity;
import org.egov.works.abstractestimate.entity.AssetsForEstimate;
import org.egov.works.abstractestimate.entity.EstimateTechnicalSanction;
import org.egov.works.abstractestimate.entity.FinancialDetail;
import org.egov.works.abstractestimate.entity.MeasurementSheet;
import org.egov.works.abstractestimate.entity.MultiYearEstimate;
import org.egov.works.abstractestimate.entity.OverheadValue;
import org.egov.works.abstractestimate.entity.SearchAbstractEstimate;
import org.egov.works.abstractestimate.entity.SearchRequestCancelEstimate;
import org.egov.works.abstractestimate.repository.AbstractEstimateRepository;
import org.egov.works.autonumber.TechnicalSanctionNumberGenerator;
import org.egov.works.config.properties.WorksApplicationProperties;
import org.egov.works.letterofacceptance.service.LetterOfAcceptanceService;
import org.egov.works.lineestimate.entity.DocumentDetails;
import org.egov.works.lineestimate.entity.LineEstimateDetails;
import org.egov.works.lineestimate.repository.LineEstimateDetailsRepository;
import org.egov.works.lineestimate.service.LineEstimateService;
import org.egov.works.master.service.NatureOfWorkService;
import org.egov.works.master.service.OverheadService;
import org.egov.works.master.service.ScheduleCategoryService;
import org.egov.works.reports.entity.WorkProgressRegister;
import org.egov.works.reports.service.WorkProgressRegisterService;
import org.egov.works.revisionestimate.entity.RevisionAbstractEstimate.RevisionEstimateStatus;
import org.egov.works.utils.WorksConstants;
import org.egov.works.utils.WorksUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class EstimateService {

    private static final Logger LOG = LoggerFactory.getLogger(EstimateService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final AbstractEstimateRepository abstractEstimateRepository;

    @Autowired
    private EstimateTechnicalSanctionService estimateTechnicalSanctionService;

    @Autowired
    private OverheadService overheadService;

    @Autowired
    private WorksUtils worksUtils;

    @Autowired
    private LetterOfAcceptanceService letterOfAcceptanceService;

    @Autowired
    private AppConfigValueService appConfigValuesService;

    @Autowired
    @Qualifier("workflowService")
    private SimpleWorkflowService<AbstractEstimate> abstractEstimateWorkflowService;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private PositionMasterService positionMasterService;

    @Autowired
    private AutonumberServiceBeanResolver beanResolver;

    @Autowired
    private AssetService assetService;

    @Autowired
    private WorkProgressRegisterService workProgressRegisterService;

    @Autowired
    private WorksApplicationProperties worksApplicationProperties;

    @Autowired
    private BoundaryService boundaryService;

    @Autowired
    private ScheduleCategoryService scheduleCategoryService;

    @Autowired
    private NatureOfWorkService natureOfWorkService;

    @Autowired
    private EgwTypeOfWorkHibernateDAO egwTypeOfWorkHibernateDAO;

    @Autowired
    private FundHibernateDAO fundHibernateDAO;

    @Autowired
    private FunctionHibernateDAO functionHibernateDAO;

    @Autowired
    private BudgetGroupDAO budgetGroupDAO;

    @Autowired
    private SchemeService schemeService;

    @Autowired
    private FinancialYearDAO financialYearDAO;

    @Autowired
    private LineEstimateService lineEstimateService;

    @Autowired
    private UOMService uomService;

    @Autowired
    private EgwStatusHibernateDAO egwStatusHibernateDAO;

    public Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }

    @Autowired
    public EstimateService(final AbstractEstimateRepository abstractEstimateRepository,
            final LineEstimateDetailsRepository lineEstimateDetailsRepository) {
        this.abstractEstimateRepository = abstractEstimateRepository;
    }

    public AbstractEstimate getAbstractEstimateById(final Long id) {
        return abstractEstimateRepository.findOne(id);
    }

    public List<AbstractEstimate> getAbstractEstimateByParentId(final Long id) {
        return abstractEstimateRepository.findByParent_idAndEgwStatus_codeEquals(id, RevisionEstimateStatus.APPROVED.toString());
    }

    @Transactional
    public AbstractEstimate createAbstractEstimate(final AbstractEstimate abstractEstimate, final MultipartFile[] files,
            final Long approvalPosition, final String approvalComent, final String additionalRule,
            final String workFlowAction) throws IOException {
        AbstractEstimate newAbstractEstimate = null;
        mergeSorAndNonSorActivities(abstractEstimate);
        final AbstractEstimate abstractEstimateFromDB = getAbstractEstimateByEstimateNumber(
                abstractEstimate.getEstimateNumber());
        if (abstractEstimateFromDB == null)
            newAbstractEstimate = saveNewAbstractEstimate(abstractEstimate);
        else
            newAbstractEstimate = updateAbstractEstimate(abstractEstimateFromDB, abstractEstimate);

        if (newAbstractEstimate.getLineEstimateDetails() != null
                && newAbstractEstimate.getLineEstimateDetails().getLineEstimate().isAbstractEstimateCreated())
            newAbstractEstimate.setEgwStatus(egwStatusHibernateDAO.getStatusByModuleAndCode(WorksConstants.ABSTRACTESTIMATE,
                    EstimateStatus.ADMIN_SANCTIONED.toString()));
        else
            createAbstractEstimateWorkflowTransition(newAbstractEstimate, approvalPosition, approvalComent, additionalRule,
                    workFlowAction);

        abstractEstimateRepository.save(newAbstractEstimate);

        final List<DocumentDetails> documentDetails = worksUtils.getDocumentDetails(files, newAbstractEstimate,
                WorksConstants.ABSTRACTESTIMATE);
        if (!documentDetails.isEmpty()) {
            newAbstractEstimate.setDocumentDetails(documentDetails);
            worksUtils.persistDocuments(documentDetails);
        }
        return newAbstractEstimate;

    }

    private AbstractEstimate saveNewAbstractEstimate(final AbstractEstimate abstractEstimate) {
        for (final MultiYearEstimate multiYearEstimate : abstractEstimate.getMultiYearEstimates())
            multiYearEstimate.setAbstractEstimate(abstractEstimate);
        for (final FinancialDetail financialDetail : abstractEstimate.getFinancialDetails())
            financialDetail.setAbstractEstimate(abstractEstimate);

        createOverheadValues(abstractEstimate);
        createAssetValues(abstractEstimate);
        for (final Activity act : abstractEstimate.getActivities())
            act.setAbstractEstimate(abstractEstimate);
        abstractEstimate.setProjectCode(abstractEstimate.getLineEstimateDetails().getProjectCode());
        return abstractEstimateRepository.save(abstractEstimate);
    }

    private void mergeSorAndNonSorActivities(final AbstractEstimate abstractEstimate) {
        for (final Activity activity : abstractEstimate.getSorActivities())
            if (activity.getId() == null) {
                activity.setAbstractEstimate(abstractEstimate);
                abstractEstimate.addActivity(activity);
            } else
                for (final Activity oldActivity : abstractEstimate.getSORActivities())
                    if (oldActivity.getId().equals(activity.getId()))
                        updateActivity(oldActivity, activity);
        for (final Activity activity : abstractEstimate.getNonSorActivities())
            if (activity.getId() == null) {
                activity.setAbstractEstimate(abstractEstimate);
                abstractEstimate.addActivity(activity);
            } else
                for (final Activity oldActivity : abstractEstimate.getNonSORActivities())
                    if (oldActivity.getId().equals(activity.getId()))
                        updateActivity(oldActivity, activity);
        if (LOG.isDebugEnabled())
            for (final Activity ac : abstractEstimate.getActivities())
                LOG.debug(ac.getMeasurementSheetList().size() + "    " + ac.getQuantity());

        for (final Activity ac : abstractEstimate.getSorActivities())
            for (final MeasurementSheet ms : ac.getMeasurementSheetList())
                if (ms.getActivity() == null)
                    ms.setActivity(ac);

        for (final Activity ac : abstractEstimate.getNonSorActivities())
            for (final MeasurementSheet ms : ac.getMeasurementSheetList())
                if (ms.getActivity() == null)
                    ms.setActivity(ac);
    }

    private List<MeasurementSheet> mergeMeasurementSheet(final Activity oldActivity, final Activity activity) {
        final List<MeasurementSheet> newMsList = new LinkedList<MeasurementSheet>(oldActivity.getMeasurementSheetList());
        for (final MeasurementSheet msnew : activity.getMeasurementSheetList()) {
            if (msnew.getId() == null) {
                msnew.setActivity(oldActivity);
                oldActivity.getMeasurementSheetList().add(msnew);
                continue;
            }

            for (final MeasurementSheet msold : oldActivity.getMeasurementSheetList())
                if (msnew.getId().longValue() == msold.getId().longValue()) {
                    msold.setLength(msnew.getLength());
                    msold.setWidth(msnew.getWidth());
                    msold.setDepthOrHeight(msnew.getDepthOrHeight());
                    msold.setNo(msnew.getNo());
                    msold.setActivity(msnew.getActivity());
                    msold.setIdentifier(msnew.getIdentifier());
                    msold.setRemarks(msnew.getRemarks());
                    msold.setSlNo(msnew.getSlNo());
                    msold.setQuantity(msnew.getQuantity());
                    newMsList.add(msold);

                }

        }
        final List<MeasurementSheet> toRemove = new LinkedList<MeasurementSheet>();
        for (final MeasurementSheet msold : oldActivity.getMeasurementSheetList()) {
            Boolean found = false;
            if (LOG.isDebugEnabled()) {
                LOG.debug(oldActivity.getMeasurementSheetList().size() + "activity.getMeasurementSheetList()");
                LOG.debug(msold.getId() + "------msold.getId()");
            }
            if (msold.getId() == null)
                continue;

            for (final MeasurementSheet msnew : activity.getMeasurementSheetList())
                if (msnew.getId() == null) {
                    // found=true;
                } else if (msnew.getId().longValue() == msold.getId().longValue()) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(msnew.getId() + "------msnew.getId()");
                        LOG.debug(msnew.getRemarks() + "------remarks");
                    }

                    found = true;
                }

            if (!found)
                toRemove.add(msold);

        }

        for (final MeasurementSheet msremove : toRemove) {
            if (LOG.isInfoEnabled())
                LOG.info("...........Removing rows....................Of MeasurementSheet" + msremove.getId());
            oldActivity.getMeasurementSheetList().remove(msremove);
        }

        return oldActivity.getMeasurementSheetList();

    }

    private void updateActivity(final Activity oldActivity, final Activity activity) {
        oldActivity.setSchedule(activity.getSchedule());
        oldActivity.setAmt(activity.getAmt());
        oldActivity.setNonSor(activity.getNonSor());
        oldActivity.setQuantity(activity.getQuantity());
        oldActivity.setRate(activity.getRate());
        oldActivity.setServiceTaxPerc(activity.getServiceTaxPerc());
        oldActivity.setEstimateRate(activity.getEstimateRate());
        oldActivity.setUom(activity.getUom());
        oldActivity.setMeasurementSheetList(mergeMeasurementSheet(oldActivity, activity));
    }

    @Transactional
    public AbstractEstimate updateAbstractEstimate(final AbstractEstimate abstractEstimateFromDB,
            final AbstractEstimate newAbstractEstimate) {
        abstractEstimateFromDB.setEstimateDate(newAbstractEstimate.getEstimateDate());
        abstractEstimateFromDB.setEstimateNumber(newAbstractEstimate.getEstimateNumber());
        abstractEstimateFromDB.setName(newAbstractEstimate.getName());
        abstractEstimateFromDB.setDescription(newAbstractEstimate.getDescription());
        abstractEstimateFromDB.setWard(newAbstractEstimate.getWard());
        abstractEstimateFromDB.setNatureOfWork(newAbstractEstimate.getNatureOfWork());
        abstractEstimateFromDB.setLocation(newAbstractEstimate.getLocation());
        abstractEstimateFromDB.setLatitude(newAbstractEstimate.getLatitude());
        abstractEstimateFromDB.setLongitude(newAbstractEstimate.getLongitude());
        abstractEstimateFromDB.setParentCategory(newAbstractEstimate.getParentCategory());
        abstractEstimateFromDB.setCategory(newAbstractEstimate.getCategory());
        abstractEstimateFromDB.setExecutingDepartment(newAbstractEstimate.getExecutingDepartment());
        abstractEstimateFromDB.setProjectCode(newAbstractEstimate.getLineEstimateDetails().getProjectCode());
        abstractEstimateFromDB.setLineEstimateDetails(newAbstractEstimate.getLineEstimateDetails());
        abstractEstimateFromDB.setEgwStatus(newAbstractEstimate.getEgwStatus());
        abstractEstimateFromDB.setApprovedBy(newAbstractEstimate.getApprovedBy());
        abstractEstimateFromDB.setApprovedDate(newAbstractEstimate.getApprovedDate());

        abstractEstimateFromDB.getMultiYearEstimates().clear();
        for (final MultiYearEstimate multiYearEstimate : newAbstractEstimate.getMultiYearEstimates()) {
            multiYearEstimate.setAbstractEstimate(abstractEstimateFromDB);
            abstractEstimateFromDB.addMultiYearEstimate(multiYearEstimate);
        }

        abstractEstimateFromDB.getActivities().clear();
        for (final Activity act : newAbstractEstimate.getActivities()) {
            act.setAbstractEstimate(abstractEstimateFromDB);
            abstractEstimateFromDB.addActivity(act);
        }

        abstractEstimateFromDB.getFinancialDetails().clear();
        for (final FinancialDetail finacilaDetail : newAbstractEstimate.getFinancialDetails()) {
            finacilaDetail.setAbstractEstimate(abstractEstimateFromDB);
            abstractEstimateFromDB.addFinancialDetails(finacilaDetail);
        }

        for (final AssetsForEstimate assetsForEstimate : newAbstractEstimate.getTempAssetValues()) {
            assetsForEstimate.setAbstractEstimate(abstractEstimateFromDB);
            assetsForEstimate.setAsset(assetService.findByCode(assetsForEstimate.getAsset().getCode()));
            abstractEstimateFromDB.addAssetValue(assetsForEstimate);
        }
        abstractEstimateFromDB.setEstimateValue(newAbstractEstimate.getEstimateValue());
        abstractEstimateFromDB.setWorkValue(newAbstractEstimate.getWorkValue());
        abstractEstimateFromDB.setCreatedDate(new Date());
        abstractEstimateFromDB.setLastModifiedDate(new Date());
        abstractEstimateFromDB.setCreatedBy(securityUtils.getCurrentUser());
        abstractEstimateFromDB.setLastModifiedBy(securityUtils.getCurrentUser());

        abstractEstimateFromDB.getOverheadValues().clear();
        for (final OverheadValue value : newAbstractEstimate.getTempOverheadValues()) {
            OverheadValue newOverheadValue = null;
            newOverheadValue = new OverheadValue();
            newOverheadValue.setOverhead(overheadService.getOverheadById(value.getOverhead().getId()));
            newOverheadValue.setAmount(value.getAmount());
            newOverheadValue.setAbstractEstimate(abstractEstimateFromDB);
            abstractEstimateFromDB.getOverheadValues().add(newOverheadValue);
        }

        abstractEstimateFromDB.getEstimateTechnicalSanctions().clear();
        for (final EstimateTechnicalSanction ets : newAbstractEstimate.getEstimateTechnicalSanctions()) {
            ets.setAbstractEstimate(abstractEstimateFromDB);
            abstractEstimateFromDB.getEstimateTechnicalSanctions().add(ets);
        }

        abstractEstimateRepository.save(abstractEstimateFromDB);
        return abstractEstimateFromDB;
    }

    @Transactional
    public AbstractEstimate createAbstractEstimateOnLineEstimateTechSanction(
            final LineEstimateDetails lineEstimateDetails, final int i) {
        final AbstractEstimate savedAbstractEstimate = abstractEstimateRepository
                .save(populateAbstractEstimate(lineEstimateDetails));
        saveTechnicalSanction(savedAbstractEstimate, i);
        return savedAbstractEstimate;
    }

    private AbstractEstimate populateAbstractEstimate(final LineEstimateDetails lineEstimateDetails) {
        final AbstractEstimate abstractEstimate = new AbstractEstimate();
        abstractEstimate.setEstimateDate(lineEstimateDetails.getLineEstimate().getLineEstimateDate());
        abstractEstimate.setEstimateNumber(lineEstimateDetails.getEstimateNumber());
        abstractEstimate.setName(lineEstimateDetails.getNameOfWork());
        abstractEstimate.setDescription(lineEstimateDetails.getNameOfWork());
        abstractEstimate.setWard(lineEstimateDetails.getLineEstimate().getWard());
        abstractEstimate.setNatureOfWork(lineEstimateDetails.getLineEstimate().getNatureOfWork());
        if (lineEstimateDetails.getLineEstimate().getLocation() != null)
            abstractEstimate.setLocation(lineEstimateDetails.getLineEstimate().getLocation().getName());
        abstractEstimate.setParentCategory(lineEstimateDetails.getLineEstimate().getTypeOfWork());
        abstractEstimate.setCategory(lineEstimateDetails.getLineEstimate().getSubTypeOfWork());
        abstractEstimate.setExecutingDepartment(lineEstimateDetails.getLineEstimate().getExecutingDepartment());
        abstractEstimate.setWorkValue(lineEstimateDetails.getActualEstimateAmount().doubleValue());
        abstractEstimate.setEstimateValue(lineEstimateDetails.getActualEstimateAmount());
        abstractEstimate.setEgwStatus(worksUtils.getStatusByModuleAndCode(WorksConstants.ABSTRACTESTIMATE,
                AbstractEstimate.EstimateStatus.ADMIN_SANCTIONED.toString()));
        abstractEstimate.setProjectCode(lineEstimateDetails.getProjectCode());
        abstractEstimate.setApprovedDate(lineEstimateDetails.getLineEstimate().getTechnicalSanctionDate());
        abstractEstimate.setLineEstimateDetails(lineEstimateDetails);
        abstractEstimate.addFinancialDetails(populateEstimateFinancialDetails(abstractEstimate));
        abstractEstimate.addMultiYearEstimate(populateMultiYearEstimate(abstractEstimate));
        return abstractEstimate;
    }

    public FinancialDetail populateEstimateFinancialDetails(final AbstractEstimate abstractEstimate) {
        final FinancialDetail financialDetail = new FinancialDetail();
        financialDetail.setAbstractEstimate(abstractEstimate);
        financialDetail.setFund(abstractEstimate.getLineEstimateDetails().getLineEstimate().getFund());
        financialDetail.setFunction(abstractEstimate.getLineEstimateDetails().getLineEstimate().getFunction());
        financialDetail.setBudgetGroup(abstractEstimate.getLineEstimateDetails().getLineEstimate().getBudgetHead());
        financialDetail.setScheme(abstractEstimate.getLineEstimateDetails().getLineEstimate().getScheme());
        financialDetail.setSubScheme(abstractEstimate.getLineEstimateDetails().getLineEstimate().getSubScheme());
        return financialDetail;
    }

    public MultiYearEstimate populateMultiYearEstimate(final AbstractEstimate abstractEstimate) {
        final MultiYearEstimate multiYearEstimate = new MultiYearEstimate();
        multiYearEstimate.setAbstractEstimate(abstractEstimate);
        multiYearEstimate.setFinancialYear(worksUtils
                .getFinancialYearByDate(abstractEstimate.getLineEstimateDetails().getLineEstimate().getLineEstimateDate()));
        multiYearEstimate.setPercentage(100);
        return multiYearEstimate;
    }

    private EstimateTechnicalSanction saveTechnicalSanction(final AbstractEstimate abstractEstimate, final int i) {
        final EstimateTechnicalSanction estimateTechnicalSanction = new EstimateTechnicalSanction();
        estimateTechnicalSanction.setAbstractEstimate(abstractEstimate);
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(abstractEstimate.getLineEstimateDetails().getLineEstimate().getTechnicalSanctionNumber());
        if (i > 0) {
            stringBuilder.append("/");
            stringBuilder.append(i);
        }
        estimateTechnicalSanction.setTechnicalSanctionNumber(stringBuilder.toString());
        estimateTechnicalSanction.setTechnicalSanctionDate(
                abstractEstimate.getLineEstimateDetails().getLineEstimate().getTechnicalSanctionDate());
        estimateTechnicalSanction.setTechnicalSanctionBy(
                abstractEstimate.getLineEstimateDetails().getLineEstimate().getTechnicalSanctionBy());

        // TODO: move to cascade save with AbstractEstimate object once
        // AbstractEstimate entity converted to JPA
        return estimateTechnicalSanctionService.save(estimateTechnicalSanction);
    }

    public AbstractEstimate getAbstractEstimateByEstimateNumber(final String estimateNumber) {
        return abstractEstimateRepository.findByEstimateNumberAndEgwStatus_codeNotLike(estimateNumber,
                AbstractEstimate.EstimateStatus.CANCELLED.toString());
    }

    public AbstractEstimate getAbstractEstimateByEstimateNumberAndStatus(final String estimateNumber) {
        return abstractEstimateRepository.findByLineEstimateDetails_EstimateNumberAndEgwStatus_codeEquals(
                estimateNumber, AbstractEstimate.EstimateStatus.ADMIN_SANCTIONED.toString());
    }

    public AbstractEstimate getAbstractEstimateByLineEstimateDetailsForCancelLineEstimate(final Long id) {
        return abstractEstimateRepository.findByLineEstimateDetails_IdAndEgwStatus_codeEquals(id,
                AbstractEstimate.EstimateStatus.ADMIN_SANCTIONED.toString());
    }

    public BigDecimal getPaymentsReleasedForLineEstimate(final LineEstimateDetails lineEstimateDetails) {
        final WorkProgressRegister workProgressRegister = workProgressRegisterService
                .getWorkProgressRegisterByLineEstimateDetailsId(lineEstimateDetails);
        return workProgressRegister != null ? workProgressRegister.getTotalBillPaidSoFar() : BigDecimal.ZERO;
    }

    public void loadModelValues(final LineEstimateDetails lineEstimateDetails, final Model model,
            final AbstractEstimate abstractEstimate) {
        model.addAttribute("paymentsReleasedSoFar", getPaymentsReleasedForLineEstimate(lineEstimateDetails));
        model.addAttribute("workOrder",
                letterOfAcceptanceService.getWorkOrderByEstimateNumber(lineEstimateDetails.getEstimateNumber()));
        final List<AppConfigValues> values = appConfigValuesService.getConfigValuesByModuleAndKey(
                WorksConstants.WORKS_MODULE_NAME, WorksConstants.APPCONFIG_KEY_SHOW_SERVICE_FIELDS);
        final AppConfigValues value = values.get(0);
        if (value.getValue().equalsIgnoreCase("Yes"))
            model.addAttribute("isServiceVATRequired", true);
        else
            model.addAttribute("isServiceVATRequired", false);
    }

    public void validateAssetDetails(final AbstractEstimate abstractEstimate, final BindingResult bindErrors) {
        if (worksApplicationProperties.assetRequired().toString().equalsIgnoreCase("Yes")) {
            final List<AppConfigValues> appConfigvalues = appConfigValuesService.getConfigValuesByModuleAndKey(
                    WorksConstants.WORKS_MODULE_NAME, WorksConstants.ASSETDETAILS_REQUIRED_FOR_ESTIMATE);
            final AppConfigValues value = appConfigvalues.get(0);
            if (value.getValue().equalsIgnoreCase("Yes") && abstractEstimate.getTempAssetValues() != null
                    && abstractEstimate.getTempAssetValues().isEmpty())
                bindErrors.reject("error.assetdetails.required", "error.assetdetails.required");

            Asset asset = null;
            Integer index = 0;
            for (final AssetsForEstimate assetsForEstimate : abstractEstimate.getTempAssetValues())
                if (assetsForEstimate != null) {
                    if (StringUtils.isBlank(assetsForEstimate.getAsset().getCode()))
                        bindErrors.rejectValue("tempAssetValues[" + index + "].asset.code", "error.assetcode.required");
                    if (StringUtils.isBlank(assetsForEstimate.getAsset().getName()))
                        bindErrors.rejectValue("tempAssetValues[" + index + "].asset.name", "error.assetname.required");
                    if (asset != null && asset.getCode().equals(assetsForEstimate.getAsset().getCode()))
                        bindErrors.rejectValue("tempAssetValues[" + index + "].asset.code", "error.asset.not.unique");
                    asset = assetsForEstimate.getAsset();
                    index++;
                }
        }
    }

    public Long getApprovalPositionByMatrixDesignation(final AbstractEstimate abstractEstimate, Long approvalPosition,
            final String additionalRule, final String mode, final String workFlowAction) {
        final WorkFlowMatrix wfmatrix = abstractEstimateWorkflowService.getWfMatrix(abstractEstimate.getStateType(),
                null, null, additionalRule, abstractEstimate.getCurrentState().getValue(), null);
        if (abstractEstimate.getEgwStatus() != null && abstractEstimate.getEgwStatus().getCode() != null)
            if ((abstractEstimate.getEgwStatus().getCode().equals(EstimateStatus.CREATED.toString()) ||
                    abstractEstimate.getEgwStatus().getCode().equals(EstimateStatus.RESUBMITTED.toString()))
                    && abstractEstimate.getState() != null)
                if (mode.equals("edit"))
                    approvalPosition = abstractEstimate.getState().getOwnerPosition().getId();
                else
                    approvalPosition = worksUtils.getApproverPosition(wfmatrix.getNextDesignation(),
                            abstractEstimate.getState(), abstractEstimate.getCreatedBy().getId());
        if (workFlowAction.equals(WorksConstants.CANCEL_ACTION)
                && wfmatrix.getNextState().equals(WorksConstants.WF_STATE_CREATED))
            approvalPosition = null;

        return approvalPosition;
    }

    @Transactional
    public AbstractEstimate updateAbstractEstimateDetails(final AbstractEstimate abstractEstimate,
            final Long approvalPosition, final String approvalComent, final String additionalRule,
            final String workFlowAction, final MultipartFile[] files, final String removedActivityIds)
            throws ValidationException, IOException {
        AbstractEstimate updatedAbstractEstimate = null;

        if ((abstractEstimate.getEgwStatus().getCode().equals(EstimateStatus.NEW.toString())
                || abstractEstimate.getEgwStatus().getCode().equals(EstimateStatus.REJECTED.toString()))
                && !workFlowAction.equals(WorksConstants.CANCEL_ACTION)) {

            for (final MultiYearEstimate multiYearEstimate : abstractEstimate.getMultiYearEstimates())
                multiYearEstimate.setAbstractEstimate(abstractEstimate);
            for (final FinancialDetail financialDetail : abstractEstimate.getFinancialDetails())
                financialDetail.setAbstractEstimate(abstractEstimate);

            createOverheadValues(abstractEstimate);

            createAssetValues(abstractEstimate);

            mergeSorAndNonSorActivities(abstractEstimate);
            List<Activity> activities = new ArrayList<Activity>(abstractEstimate.getActivities());
            activities = removeDeletedActivities(activities, removedActivityIds);
            abstractEstimate.setActivities(activities);
            for (final Activity activity : abstractEstimate.getActivities())
                activity.setAbstractEstimate(abstractEstimate);

            final List<DocumentDetails> documentDetails = worksUtils.getDocumentDetails(files, abstractEstimate,
                    WorksConstants.ABSTRACTESTIMATE);
            if (!documentDetails.isEmpty()) {
                abstractEstimate.setDocumentDetails(documentDetails);
                worksUtils.persistDocuments(documentDetails);
            }
        } else {
            if ((abstractEstimate.getEgwStatus().getCode().equals(EstimateStatus.CREATED.toString()) ||
                    abstractEstimate.getEgwStatus().getCode().equals(EstimateStatus.RESUBMITTED.toString()))
                    && workFlowAction.equals(WorksConstants.SUBMIT_ACTION))
                saveTechnicalSanctionDetails(abstractEstimate);

            if (abstractEstimate.getEgwStatus().getCode().equals(EstimateStatus.TECH_SANCTIONED.toString())
                    && workFlowAction.equalsIgnoreCase(WorksConstants.ACTION_APPROVE))
                saveAdminSanctionDetails(abstractEstimate);
        }

        updatedAbstractEstimate = abstractEstimateRepository.save(abstractEstimate);

        abstractEstimateStatusChange(updatedAbstractEstimate, workFlowAction);

        createAbstractEstimateWorkflowTransition(updatedAbstractEstimate, approvalPosition, approvalComent,
                additionalRule, workFlowAction);

        abstractEstimateRepository.save(updatedAbstractEstimate);

        return updatedAbstractEstimate;
    }

    private void createOverheadValues(final AbstractEstimate abstractEstimate) {
        OverheadValue newOverheadValue = null;
        abstractEstimate.getOverheadValues().clear();
        for (final OverheadValue overheadValue : abstractEstimate.getTempOverheadValues()) {
            newOverheadValue = new OverheadValue();
            newOverheadValue.setOverhead(overheadService.getOverheadById(overheadValue.getOverhead().getId()));
            newOverheadValue.setAmount(overheadValue.getAmount());
            newOverheadValue.setAbstractEstimate(abstractEstimate);
            abstractEstimate.getOverheadValues().add(newOverheadValue);
        }
    }

    private void createAssetValues(final AbstractEstimate abstractEstimate) {
        AssetsForEstimate assetForEstimate = null;
        abstractEstimate.getAssetValues().clear();
        for (final AssetsForEstimate assetEstimate : abstractEstimate.getTempAssetValues()) {
            assetForEstimate = new AssetsForEstimate();
            assetForEstimate.setAbstractEstimate(abstractEstimate);
            assetForEstimate.setAsset(assetService.findByCode(assetEstimate.getAsset().getCode()));
            abstractEstimate.getAssetValues().add(assetForEstimate);
        }
    }

    private List<Activity> removeDeletedActivities(final List<Activity> activities, final String removedActivityIds) {
        final List<Activity> activityList = new ArrayList<Activity>();
        if (null != removedActivityIds) {
            final String[] ids = removedActivityIds.split(",");
            final List<String> strList = new ArrayList<String>();
            for (final String str : ids)
                strList.add(str);
            for (final Activity activity : activities)
                if (activity.getId() != null) {
                    if (!strList.contains(activity.getId().toString()))
                        activityList.add(activity);
                } else
                    activityList.add(activity);
        } else
            return activities;
        return activityList;
    }

    private void saveAdminSanctionDetails(final AbstractEstimate abstractEstimate) {
        abstractEstimate.setApprovedBy(securityUtils.getCurrentUser());
        abstractEstimate.setApprovedDate(new Date());
    }

    private void saveTechnicalSanctionDetails(final AbstractEstimate abstractEstimate) {
        final EstimateTechnicalSanction estimateTechnicalSanction = new EstimateTechnicalSanction();
        estimateTechnicalSanction.setAbstractEstimate(abstractEstimate);
        estimateTechnicalSanction.setTechnicalSanctionBy(securityUtils.getCurrentUser());
        estimateTechnicalSanction.setTechnicalSanctionDate(new Date());
        final TechnicalSanctionNumberGenerator tsng = beanResolver
                .getAutoNumberServiceFor(TechnicalSanctionNumberGenerator.class);
        estimateTechnicalSanction.setTechnicalSanctionNumber(tsng.getNextNumber(abstractEstimate));
        abstractEstimate.getEstimateTechnicalSanctions().add(estimateTechnicalSanction);
    }

    public void abstractEstimateStatusChange(final AbstractEstimate abstractEstimate, final String workFlowAction) {
        if (null != abstractEstimate && null != abstractEstimate.getEgwStatus()
                && null != abstractEstimate.getEgwStatus().getCode())
            if (workFlowAction.equals(WorksConstants.SAVE_ACTION))
                abstractEstimate.setEgwStatus(worksUtils.getStatusByModuleAndCode(WorksConstants.ABSTRACTESTIMATE,
                        EstimateStatus.NEW.toString()));
            else if (workFlowAction.equals(WorksConstants.CANCEL_ACTION)
                    && abstractEstimate.getEgwStatus().getCode().equals(EstimateStatus.NEW.toString()))
                abstractEstimate.setEgwStatus(worksUtils.getStatusByModuleAndCode(WorksConstants.ABSTRACTESTIMATE,
                        EstimateStatus.CANCELLED.toString()));
            else if (abstractEstimate.getEgwStatus().getCode().equals(EstimateStatus.NEW.toString()))
                abstractEstimate.setEgwStatus(worksUtils.getStatusByModuleAndCode(WorksConstants.ABSTRACTESTIMATE,
                        EstimateStatus.CREATED.toString()));
            else if (abstractEstimate.getEgwStatus().getCode().equals(EstimateStatus.CREATED.toString())
                    && abstractEstimate.getState() != null && workFlowAction.equals(WorksConstants.SUBMIT_ACTION))
                abstractEstimate.setEgwStatus(worksUtils.getStatusByModuleAndCode(WorksConstants.ABSTRACTESTIMATE,
                        EstimateStatus.TECH_SANCTIONED.toString()));
            else if (abstractEstimate.getEgwStatus().getCode().equals(EstimateStatus.RESUBMITTED.toString())
                    && abstractEstimate.getState() != null && workFlowAction.equals(WorksConstants.SUBMIT_ACTION))
                abstractEstimate.setEgwStatus(worksUtils.getStatusByModuleAndCode(WorksConstants.ABSTRACTESTIMATE,
                        EstimateStatus.TECH_SANCTIONED.toString()));
            else if (abstractEstimate.getEgwStatus().getCode().equals(EstimateStatus.TECH_SANCTIONED.toString())
                    && !workFlowAction.equals(WorksConstants.REJECT_ACTION))
                abstractEstimate.setEgwStatus(worksUtils.getStatusByModuleAndCode(WorksConstants.ABSTRACTESTIMATE,
                        EstimateStatus.ADMIN_SANCTIONED.toString()));
            else if (workFlowAction.equals(WorksConstants.REJECT_ACTION))
                abstractEstimate.setEgwStatus(worksUtils.getStatusByModuleAndCode(WorksConstants.ABSTRACTESTIMATE,
                        EstimateStatus.REJECTED.toString()));
            else if (abstractEstimate.getEgwStatus().getCode().equals(EstimateStatus.REJECTED.toString())
                    && workFlowAction.equals(WorksConstants.CANCEL_ACTION))
                abstractEstimate.setEgwStatus(worksUtils.getStatusByModuleAndCode(WorksConstants.ABSTRACTESTIMATE,
                        EstimateStatus.CANCELLED.toString()));
            else if (abstractEstimate.getEgwStatus().getCode().equals(EstimateStatus.REJECTED.toString())
                    && workFlowAction.equals(WorksConstants.FORWARD_ACTION))
                abstractEstimate.setEgwStatus(worksUtils.getStatusByModuleAndCode(WorksConstants.ABSTRACTESTIMATE,
                        EstimateStatus.RESUBMITTED.toString()));
    }

    public void createAbstractEstimateWorkflowTransition(final AbstractEstimate abstractEstimate,
            final Long approvalPosition, final String approvalComent, final String additionalRule,
            final String workFlowAction) {
        if (LOG.isDebugEnabled())
            LOG.debug(" Create WorkFlow Transition Started  ...");
        final User user = securityUtils.getCurrentUser();
        final DateTime currentDate = new DateTime();
        final Assignment userAssignment = assignmentService.getPrimaryAssignmentForUser(user.getId());
        Position pos = null;
        Assignment wfInitiator = null;
        final String currState = "";
        final String natureOfwork = WorksConstants.WORKFLOWTYPE_DISPLAYNAME_ESTIMATE;
        WorkFlowMatrix wfmatrix = null;

        if (null != abstractEstimate.getId())
            wfInitiator = assignmentService.getPrimaryAssignmentForUser(abstractEstimate.getCreatedBy().getId());
        if (WorksConstants.REJECT_ACTION.toString().equalsIgnoreCase(workFlowAction)) {
            if (wfInitiator.equals(userAssignment))
                abstractEstimate.transition(true).end().withSenderName(user.getUsername() + "::" + user.getName())
                        .withComments(approvalComent).withDateInfo(currentDate.toDate()).withNatureOfTask(natureOfwork);
            else
                abstractEstimate.transition(true).withSenderName(user.getUsername() + "::" + user.getName())
                        .withComments(approvalComent).withStateValue(WorksConstants.WF_STATE_REJECTED)
                        .withDateInfo(currentDate.toDate()).withOwner(wfInitiator.getPosition()).withNextAction("")
                        .withNatureOfTask(natureOfwork);
        } else if (WorksConstants.SAVE_ACTION.toString().equalsIgnoreCase(workFlowAction)) {
            wfmatrix = abstractEstimateWorkflowService.getWfMatrix(abstractEstimate.getStateType(), null, null,
                    additionalRule, WorksConstants.NEW, null);
            if (abstractEstimate.getState() == null)
                abstractEstimate.transition(true).start().withSenderName(user.getUsername() + "::" + user.getName())
                        .withComments(approvalComent).withStateValue(WorksConstants.NEW)
                        .withDateInfo(currentDate.toDate()).withOwner(wfInitiator.getPosition())
                        .withNextAction(WorksConstants.ESTIMATE_ONSAVE_NEXTACTION_VALUE).withNatureOfTask(natureOfwork);
        } else {
            if (null != approvalPosition && approvalPosition != -1 && !approvalPosition.equals(Long.valueOf(0)))
                pos = positionMasterService.getPositionById(approvalPosition);
            if (null == abstractEstimate.getState()) {
                wfmatrix = abstractEstimateWorkflowService.getWfMatrix(abstractEstimate.getStateType(), null, null,
                        additionalRule, currState, null);
                abstractEstimate.transition().start().withSenderName(user.getUsername() + "::" + user.getName())
                        .withComments(approvalComent).withStateValue(wfmatrix.getNextState()).withDateInfo(new Date())
                        .withOwner(pos).withNextAction(wfmatrix.getNextAction()).withNatureOfTask(natureOfwork);
            } else if (WorksConstants.CANCEL_ACTION.toString().equalsIgnoreCase(workFlowAction)) {
                final String stateValue = WorksConstants.WF_STATE_CANCELLED;
                wfmatrix = abstractEstimateWorkflowService.getWfMatrix(abstractEstimate.getStateType(), null, null,
                        additionalRule, abstractEstimate.getCurrentState().getValue(), null);
                abstractEstimate.transition(true).withSenderName(user.getUsername() + "::" + user.getName())
                        .withComments(approvalComent).withStateValue(stateValue).withDateInfo(currentDate.toDate())
                        .withOwner(pos).withNextAction("").withNatureOfTask(natureOfwork);
            } else {
                wfmatrix = abstractEstimateWorkflowService.getWfMatrix(abstractEstimate.getStateType(), null, null,
                        additionalRule, abstractEstimate.getCurrentState().getValue(), null);
                abstractEstimate.transition(true).withSenderName(user.getUsername() + "::" + user.getName())
                        .withComments(approvalComent).withStateValue(wfmatrix.getNextState())
                        .withDateInfo(currentDate.toDate()).withOwner(pos).withNextAction(wfmatrix.getNextAction())
                        .withNatureOfTask(natureOfwork);
            }
        }
        if (LOG.isDebugEnabled())
            LOG.debug(" WorkFlow Transition Completed  ...");
    }

    public List<String> getAbstractEstimateByEstimateNumberLike(final String estimateNumber) {
        return abstractEstimateRepository.findDistinctEstimateNumberContainingIgnoreCase("%" + estimateNumber + "%");
    }

    public List<User> getAbstractEstimateCreatedByUsers() {
        return abstractEstimateRepository.findAbstractEstimateCreatedByUsers();
    }

    public List<AbstractEstimate> searchAbstractEstimates(final SearchAbstractEstimate searchAbstractEstimate) {
        if (searchAbstractEstimate != null) {
            final Criteria criteria = entityManager.unwrap(Session.class).createCriteria(AbstractEstimate.class)
                    .createAlias("egwStatus", "status")
                    .createAlias("projectCode", "pc");
            criteria.add(Restrictions.isNull("parent.id"));
            if (searchAbstractEstimate.getAbstractEstimateNumber() != null)
                criteria.add(Restrictions.eq("estimateNumber", searchAbstractEstimate.getAbstractEstimateNumber()));
            if (searchAbstractEstimate.getDepartment() != null)
                criteria.add(Restrictions.eq("executingDepartment.id", searchAbstractEstimate.getDepartment()));
            if (searchAbstractEstimate.getWorkIdentificationNumber() != null)
                criteria.add(Restrictions.ilike("pc.code", searchAbstractEstimate.getWorkIdentificationNumber(),
                        MatchMode.ANYWHERE));
            if (searchAbstractEstimate.getStatus() != null)
                criteria.add(Restrictions.eq("status.id", Integer.valueOf(searchAbstractEstimate.getStatus())));
            if (searchAbstractEstimate.getCreatedBy() != null)
                criteria.add(Restrictions.eq("createdBy.id", searchAbstractEstimate.getCreatedBy()));
            if (searchAbstractEstimate.getFromDate() != null)
                criteria.add(Restrictions.ge("estimateDate", searchAbstractEstimate.getFromDate()));
            if (searchAbstractEstimate.getToDate() != null)
                criteria.add(Restrictions.le("estimateDate", searchAbstractEstimate.getToDate()));

            criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
            return criteria.list();
        } else
            return new ArrayList<AbstractEstimate>();
    }

    public List<User> getAbstractEstimateCreatedByUsers(final List<Long> departmentIds) {
        return abstractEstimateRepository.findAbstractEstimateCreatedByUsers(departmentIds);
    }

    public List<AbstractEstimate> searchAbstractEstimatesForLoa(
            final AbstractEstimateForLoaSearchRequest abstractEstimateForLoaSearchRequest) {
        List<AbstractEstimate> abstractEstimates = new ArrayList<AbstractEstimate>();
        final StringBuilder queryStr = new StringBuilder(500);
        queryStr.append(
                "select distinct(estimate) from AbstractEstimate estimate where estimate.parent.id is null and estimate.egwStatus.code = :aeStatus and not exists (select distinct(woe.estimate) from WorkOrderEstimate as woe where estimate.id = woe.estimate.id and upper(woe.workOrder.egwStatus.code) != upper(:woStatus) and upper(estimate.egwStatus.code) = upper(:aeStatus))");

        queryStr.append(
                " and exists (select act.abstractEstimate from Activity as act where estimate.id = act.abstractEstimate.id )");

        queryStr.append(
                " and exists (select off.id from OfflineStatus as off where off.objectId = estimate.id and off.objectType = :objectType and upper(off.egwStatus.code) = upper(:offStatus) )");

        if (abstractEstimateForLoaSearchRequest != null) {
            if (abstractEstimateForLoaSearchRequest.getAdminSanctionNumber() != null)
                queryStr.append(
                        " and upper(estimate.lineEstimateDetails.lineEstimate.adminSanctionNumber) like upper(:adminSanctionNumber)");
            if (abstractEstimateForLoaSearchRequest.getExecutingDepartment() != null)
                queryStr.append(" and estimate.executingDepartment.id = :departmentId");
            if (abstractEstimateForLoaSearchRequest.getEstimateNumber() != null)
                queryStr.append(" and upper(estimate.estimateNumber) = upper(:estimateNumber)");
            if (abstractEstimateForLoaSearchRequest.getAdminSanctionFromDate() != null)
                queryStr.append(" and estimate.approvedDate >= :fromDate");
            if (abstractEstimateForLoaSearchRequest.getAdminSanctionToDate() != null)
                queryStr.append(" and estimate.approvedDate <= :toDate");
            if (abstractEstimateForLoaSearchRequest.getAbstractEstimateCreatedBy() != null)
                queryStr.append(
                        " and estimate.createdBy.id = :createdById");
            if (abstractEstimateForLoaSearchRequest.getWorkIdentificationNumber() != null)
                queryStr.append(" and upper(estimate.projectCode.code) = upper(:projectCode)");
        }
        queryStr.append(" and estimate.lineEstimateDetails.lineEstimate.spillOverFlag = :spillOverFlag");
        final Query query = setQueryParametersForModifyLOA(abstractEstimateForLoaSearchRequest, queryStr);
        abstractEstimates = query.getResultList();
        return abstractEstimates;
    }

    private Query setQueryParametersForModifyLOA(final AbstractEstimateForLoaSearchRequest abstractEstimateForLoaSearchRequest,
            final StringBuilder queryStr) {
        final Query qry = entityManager.createQuery(queryStr.toString());
        if (abstractEstimateForLoaSearchRequest != null) {
            if (abstractEstimateForLoaSearchRequest.getAdminSanctionNumber() != null)
                qry.setParameter("adminSanctionNumber", "%" + abstractEstimateForLoaSearchRequest.getAdminSanctionNumber() + "%");
            if (abstractEstimateForLoaSearchRequest.getExecutingDepartment() != null)
                qry.setParameter("departmentId", abstractEstimateForLoaSearchRequest.getExecutingDepartment());
            if (abstractEstimateForLoaSearchRequest.getEstimateNumber() != null)
                qry.setParameter("estimateNumber", abstractEstimateForLoaSearchRequest.getEstimateNumber());
            if (abstractEstimateForLoaSearchRequest.getAdminSanctionFromDate() != null)
                qry.setParameter("fromDate", abstractEstimateForLoaSearchRequest.getAdminSanctionFromDate());
            if (abstractEstimateForLoaSearchRequest.getAdminSanctionToDate() != null)
                qry.setParameter("toDate", abstractEstimateForLoaSearchRequest.getAdminSanctionToDate());
            if (abstractEstimateForLoaSearchRequest.getAbstractEstimateCreatedBy() != null)
                qry.setParameter("createdById", abstractEstimateForLoaSearchRequest.getAbstractEstimateCreatedBy());
            if (abstractEstimateForLoaSearchRequest.getWorkIdentificationNumber() != null)
                qry.setParameter("projectCode", abstractEstimateForLoaSearchRequest.getWorkIdentificationNumber());
            qry.setParameter("spillOverFlag", abstractEstimateForLoaSearchRequest.isSpillOverFlag());
            qry.setParameter("woStatus", WorksConstants.CANCELLED_STATUS);
            qry.setParameter("aeStatus", AbstractEstimate.EstimateStatus.ADMIN_SANCTIONED.toString());
            qry.setParameter("objectType", WorksConstants.ABSTRACTESTIMATE);
            qry.setParameter("offStatus", AbstractEstimate.OfflineStatusesForAbstractEstimate.L1_TENDER_FINALIZED.toString());
        }
        return qry;
    }

    public List<AbstractEstimateForLoaSearchResult> searchAbstractEstimatesForLOA(
            final AbstractEstimateForLoaSearchRequest abstractEstimateForLoaSearchRequest) {
        final List<AbstractEstimate> abstractEstimates = searchAbstractEstimatesForLoa(
                abstractEstimateForLoaSearchRequest);
        final List<AbstractEstimateForLoaSearchResult> abstractEstimateForLoaSearchResults = new ArrayList<AbstractEstimateForLoaSearchResult>();
        for (final AbstractEstimate ae : abstractEstimates) {
            final AbstractEstimateForLoaSearchResult result = new AbstractEstimateForLoaSearchResult();
            if (ae.getLineEstimateDetails() != null)
                result.setAdminSanctionNumber(ae.getLineEstimateDetails().getLineEstimate().getAdminSanctionNumber());
            result.setAeId(ae.getId());
            result.setCreatedBy(ae.getCreatedBy().getName());
            result.setEstimateAmount(ae.getEstimateValue());
            result.setEstimateNumber(ae.getEstimateNumber());
            result.setNameOfWork(ae.getName());
            if (ae.getApprovedBy() != null)
                result.setAdminSanctionBy(ae.getApprovedBy().getName());
            result.setWorkIdentificationNumber(ae.getProjectCode().getCode());
            abstractEstimateForLoaSearchResults.add(result);
        }
        return abstractEstimateForLoaSearchResults;
    }

    public void validateActivities(final AbstractEstimate abstractEstimate, final BindingResult errors) {
        for (int i = 0; i < abstractEstimate.getSorActivities().size() - 1; i++)
            for (int j = i + 1; j < abstractEstimate.getSorActivities().size(); j++)
                if (abstractEstimate.getSorActivities().get(i).getSchedule() != null && abstractEstimate.getSorActivities().get(i)
                        .getSchedule().getId().equals(abstractEstimate.getSorActivities().get(j).getSchedule().getId())) {
                    errors.reject("error.sor.duplicate", "error.sor.duplicate");
                    break;
                }

        for (final Activity activity : abstractEstimate.getSorActivities()) {
            if (activity.getQuantity() <= 0)
                errors.reject("error.quantity.zero", "error.quantity.zero");
            if (activity.getRate() <= 0)
                errors.reject("error.rates.zero", "error.rates.zero");
        }

        for (final Activity activity : abstractEstimate.getNonSorActivities()) {
            if (activity.getQuantity() <= 0)
                errors.reject("error.quantity.zero", "error.quantity.zero");
            if (activity.getRate() <= 0)
                errors.reject("error.rates.zero", "error.rates.zero");
        }
    }

    public void validateOverheads(final AbstractEstimate abstractEstimate, final BindingResult errors) {
        for (final OverheadValue value : abstractEstimate.getTempOverheadValues())
            if (value.getOverhead().getId() == null) {
                errors.reject("error.overhead.null", "error.overhead.null");
                break;
            }
        for (final OverheadValue value : abstractEstimate.getTempOverheadValues())
            if (value.getAmount() <= 0) {
                errors.reject("error.overhead.amount", "error.overhead.amount");
                break;
            }
    }

    public void validateMultiYearEstimates(final AbstractEstimate abstractEstimate, final BindingResult bindErrors) {
        CFinancialYear cFinancialYear = null;
        Double totalPercentage = 0d;
        Integer index = 0;
        for (final MultiYearEstimate multiYearEstimate : abstractEstimate.getMultiYearEstimates()) {
            totalPercentage = totalPercentage + multiYearEstimate.getPercentage();

            if (multiYearEstimate.getFinancialYear() == null)
                bindErrors.rejectValue("multiYearEstimates[" + index + "].financialYear", "error.finyear.required");
            if (multiYearEstimate.getPercentage() == 0)
                bindErrors.rejectValue("multiYearEstimates[" + index + "].percentage", "error.percentage.required");
            if (cFinancialYear != null && cFinancialYear.equals(multiYearEstimate.getFinancialYear()))
                bindErrors.rejectValue("multiYearEstimates[" + index + "].financialYear", "error.financialYear.unique");
            if (totalPercentage > 100)
                bindErrors.rejectValue("multiYearEstimates[" + index + "].percentage", "error.percentage.greater");
            cFinancialYear = multiYearEstimate.getFinancialYear();
            index++;
        }

    }

    public void validateMandatory(final AbstractEstimate abstractEstimate, final BindingResult bindErrors) {
        if (StringUtils.isBlank(abstractEstimate.getDescription()))
            bindErrors.rejectValue("description", "error.description.required");
        final LineEstimateDetails lineEstimateDetails = abstractEstimate.getLineEstimateDetails();
        if (abstractEstimate.getEstimateValue() != null
                && abstractEstimate.getEstimateValue().compareTo(lineEstimateDetails.getEstimateAmount()) == 1) {
            final BigDecimal diffValue = abstractEstimate.getEstimateValue()
                    .subtract(lineEstimateDetails.getEstimateAmount());
            bindErrors.reject("error.estimatevalue.greater",
                    new String[] { diffValue.toString(), lineEstimateDetails.getEstimateAmount().setScale(2, BigDecimal.ROUND_UP)
                            .toString() },
                    "error.estimatevalue.greater");
        }
    }

    public void setDropDownValues(final Model model) {
        model.addAttribute("exceptionaluoms", worksUtils.getExceptionalUOMS());
        model.addAttribute("overheads", overheadService.getOverheadsByDate(new Date()));
        model.addAttribute("locations", boundaryService.getActiveBoundariesByBndryTypeNameAndHierarchyTypeName(
                WorksConstants.LOCATION_BOUNDARYTYPE, WorksConstants.LOCATION_HIERARCHYTYPE));
        model.addAttribute("scheduleCategories", scheduleCategoryService.getAllScheduleCategories());
        model.addAttribute("funds", fundHibernateDAO.findAllActiveFunds());
        model.addAttribute("functions", functionHibernateDAO.getAllActiveFunctions());
        model.addAttribute("budgetHeads", budgetGroupDAO.getBudgetGroupList());
        model.addAttribute("schemes", schemeService.findAll());
        model.addAttribute("departments", lineEstimateService.getUserDepartments(securityUtils.getCurrentUser()));
        model.addAttribute("typeOfWork", egwTypeOfWorkHibernateDAO.getTypeOfWorkForPartyTypeContractor());
        model.addAttribute("natureOfWork", natureOfWorkService.findAll());
        model.addAttribute("finYear", financialYearDAO.findAll());
        model.addAttribute("uoms", uomService.findAll());

        final List<AppConfigValues> values = appConfigValuesService.getConfigValuesByModuleAndKey(
                WorksConstants.WORKS_MODULE_NAME, WorksConstants.APPCONFIG_KEY_SHOW_SERVICE_FIELDS);
        final AppConfigValues value = values.get(0);
        if (value.getValue().equalsIgnoreCase("Yes"))
            model.addAttribute("isServiceVATRequired", true);
        else
            model.addAttribute("isServiceVATRequired", false);
        loadLocationAppConfigValue(model);

    }

    public void validateTechnicalSanctionDetail(final AbstractEstimate abstractEstimate, final BindingResult errors) {
        if (abstractEstimate.getEstimateTechnicalSanctions() != null
                && abstractEstimate.getEstimateTechnicalSanctions().get(0).getTechnicalSanctionDate() == null)
            errors.reject("error.techdate.notnull", "error.techdate.notnull");
        if (abstractEstimate.getEstimateTechnicalSanctions() != null
                && abstractEstimate.getEstimateTechnicalSanctions().get(0).getTechnicalSanctionDate() != null
                && abstractEstimate.getEstimateTechnicalSanctions().get(0).getTechnicalSanctionDate()
                        .before(abstractEstimate.getEstimateDate()))
            errors.reject("error.abstracttechnicalsanctiondate", "error.abstracttechnicalsanctiondate");
        if (abstractEstimate.getEstimateTechnicalSanctions() != null
                && abstractEstimate.getEstimateTechnicalSanctions().get(0).getTechnicalSanctionNumber() == null)
            errors.reject("error.technumber.notnull", "error.technumber.notnull");
        if (abstractEstimate.getEstimateTechnicalSanctions() != null
                && abstractEstimate.getEstimateTechnicalSanctions().get(0).getTechnicalSanctionBy() == null)
            errors.reject("error.techby.notnull", "error.techby.notnull");
        if (abstractEstimate.getEstimateTechnicalSanctions() != null
                && abstractEstimate.getEstimateTechnicalSanctions().get(0).getTechnicalSanctionNumber() != null) {
            final AbstractEstimate esistingAbstractEstimate = abstractEstimateRepository
                    .findByEstimateTechnicalSanctionsIgnoreCase_TechnicalSanctionNumberAndEgwStatus_CodeNot(
                            abstractEstimate.getEstimateTechnicalSanctions().get(0)
                                    .getTechnicalSanctionNumber(),
                            EstimateStatus.CANCELLED.toString());
            if (esistingAbstractEstimate != null)
                errors.reject("error.technumber.unique", "error.technumber.unique");
        }
        if (abstractEstimate.getEstimateDate() == null)
            errors.reject("errors.abbstractestimate.estimatedate", "errors.abbstractestimate.estimatedate");
        if (abstractEstimate.getLineEstimateDetails() != null
                && abstractEstimate.getEstimateDate() != null && abstractEstimate.getEstimateDate()
                        .before(abstractEstimate.getLineEstimateDetails().getLineEstimate().getAdminSanctionDate()))
            errors.reject("error.abstractadminsanctiondatele", "error.abstractadminsanctiondatele");
    }

    public void validateAdminSanctionDetail(final AbstractEstimate abstractEstimate, final BindingResult errors) {
        if (abstractEstimate.getApprovedDate() == null)
            errors.reject("error.adminsanctiondate.notnull", "error.adminsanctiondate.notnull");
        if (abstractEstimate.getApprovedBy() == null)
            errors.reject("error.adminby.notnull", "error.adminby.notnull");
        if (abstractEstimate.getApprovedDate() != null
                && abstractEstimate.getEstimateDate().after(abstractEstimate.getApprovedDate()))
            errors.reject("error.abstractadminsanctiondate", "error.abstractadminsanctiondate");

    }

    public void setTechnicalSanctionDetails(final AbstractEstimate abstractEstimate) {
        if (abstractEstimate.getEstimateTechnicalSanctions() != null)
            abstractEstimate.getEstimateTechnicalSanctions().get(0).setAbstractEstimate(abstractEstimate);
    }

    public List<String> getAbstractEstimateNumbersToCancelLineEstimate(final Long lineEstimateId) {
        final List<String> estimateNumbers = abstractEstimateRepository
                .findAbstractEstimateNumbersToCancelLineEstimate(lineEstimateId,
                        AbstractEstimate.EstimateStatus.CANCELLED.toString());
        return estimateNumbers;
    }

    @Transactional
    public AbstractEstimate cancel(final AbstractEstimate abstractEstimate) {
        abstractEstimate.setEgwStatus(worksUtils.getStatusByModuleAndCode(WorksConstants.ABSTRACTESTIMATE,
                AbstractEstimate.EstimateStatus.CANCELLED.toString()));
        final AbstractEstimate savedAbstractEstimate = abstractEstimateRepository.save(abstractEstimate);
        return savedAbstractEstimate;
    }

    public List<AbstractEstimate> searchEstimatesToCancel(final SearchRequestCancelEstimate searchRequestCancelEstimate) {
        final StringBuilder queryStr = new StringBuilder(500);
        queryStr.append(
                "select distinct(ae) from AbstractEstimate ae where exists (select distinct(activity.id) from Activity activity where activity.abstractEstimate.id = ae.id) and not exists (select distinct(woe) from WorkOrderEstimate as woe where woe.estimate.id = ae.id and woe.workOrder.egwStatus.code != :workOrderStatus) ");
        
        queryStr.append(" and ae.parent.id is null ");
            
        if (searchRequestCancelEstimate != null) {
            if (searchRequestCancelEstimate.getEstimateNumber() != null)
                queryStr.append(" and upper(ae.estimateNumber) = upper(:estimateNumber)");
            if (searchRequestCancelEstimate.getLineEstimateNumber() != null)
                queryStr.append(
                        " and upper(ae.lineEstimateDetails.lineEstimate.lineEstimateNumber) like upper(:lineEstimateNumber)");
            if (searchRequestCancelEstimate.getWinCode() != null)
                queryStr.append(" and upper(ae.projectCode.code) like upper(:projectCode)");
            if (searchRequestCancelEstimate.getStatus() != null)
                queryStr.append(" and upper(ae.egwStatus.code) = upper(:status)");
            if (searchRequestCancelEstimate.getFromDate() != null)
                queryStr.append(" and ae.estimateDate >= :fromDate");
            if (searchRequestCancelEstimate.getToDate() != null)
                queryStr.append(" and ae.estimateDate <= :toDate");
        }

        final Query query = setQueryParametersForAbstractEstimate(searchRequestCancelEstimate, queryStr);
        return query.getResultList();
    }

    private Query setQueryParametersForAbstractEstimate(final SearchRequestCancelEstimate searchRequestCancelEstimate,
            final StringBuilder queryStr) {
        final Query qry = entityManager.createQuery(queryStr.toString());
        if (searchRequestCancelEstimate != null) {
            if (searchRequestCancelEstimate.getEstimateNumber() != null)
                qry.setParameter("estimateNumber", searchRequestCancelEstimate.getEstimateNumber());
            if (searchRequestCancelEstimate.getLineEstimateNumber() != null)
                qry.setParameter("lineEstimateNumber", "%" + searchRequestCancelEstimate.getLineEstimateNumber() + "%");
            if (searchRequestCancelEstimate.getWinCode() != null)
                qry.setParameter("projectCode", "%" + searchRequestCancelEstimate.getWinCode() + "%");
            if (searchRequestCancelEstimate.getStatus() != null)
                qry.setParameter("status", AbstractEstimate.EstimateStatus.ADMIN_SANCTIONED.toString());
            if (searchRequestCancelEstimate.getFromDate() != null)
                qry.setParameter("fromDate", searchRequestCancelEstimate.getFromDate());
            if (searchRequestCancelEstimate.getToDate() != null)
                qry.setParameter("toDate", searchRequestCancelEstimate.getToDate());
        }
        qry.setParameter("workOrderStatus", WorksConstants.CANCELLED_STATUS);
        return qry;
    }

    public List<String> findEstimateNumbersToCancelEstimate(final String code) {
        final List<String> estimateNumbers = abstractEstimateRepository
                .findAbstractEstimateNumbersToCancelEstimate("%" + code + "%",
                        AbstractEstimate.EstimateStatus.CANCELLED.toString());
        return estimateNumbers;
    }

    public List<AbstractEstimate> searchAbstractEstimatesForOfflineStatus(
            final AbstractEstimateForLoaSearchRequest abstractEstimateForLoaSearchRequest) {
        List<AbstractEstimate> abstractEstimateList = new ArrayList<AbstractEstimate>();
        final StringBuilder queryStr = new StringBuilder(500);
        queryStr.append(
                "select distinct(ae) from AbstractEstimate ae where ae.parent is null and ae.egwStatus.code =:abstractEstimateStatus and not exists (select distinct(woe.estimate) from WorkOrderEstimate as woe where woe.estimate.id = ae.id and woe.workOrder.egwStatus.code != :workOrderStatus )");
        if (abstractEstimateForLoaSearchRequest != null) {
            if (abstractEstimateForLoaSearchRequest.getAbstractEstimateNumber() != null)
                queryStr.append(" and upper(ae.estimateNumber) =:abstractEstimateNumber");
            if (abstractEstimateForLoaSearchRequest.getAdminSanctionFromDate() != null)
                queryStr.append(" and ae.approvedDate >= :abstractEstimateFromDate");
            if (abstractEstimateForLoaSearchRequest.getAdminSanctionToDate() != null)
                queryStr.append(" and ae.approvedDate <= :abstractEstimateToDate");

            if (abstractEstimateForLoaSearchRequest.getAbstractEstimateCreatedBy() != null)
                queryStr.append(" and ae.createdBy.id = :abstractEstimateCreatedBy");

            if (abstractEstimateForLoaSearchRequest.getEgwStatus() != null)
                if (abstractEstimateForLoaSearchRequest.getEgwStatus().equals(WorksConstants.ADMIN_SANCTIONED_STATUS))
                    queryStr.append(
                            " and not exists (select distinct(os.objectId) from OfflineStatus as os where os.objectType = :objectType and ae.id = os.objectId )");
                else if (abstractEstimateForLoaSearchRequest.getEgwStatus() != null)
                    queryStr.append(
                            " and ae.id = (select distinct(os.objectId) from OfflineStatus as os where os.id = (select max(status.id) from OfflineStatus status where status.objectType = :objectType and status.objectId = ae.id) and os.objectId = ae.id and lower(os.egwStatus.code) = :offlineStatus and os.objectType = :objectType )");
        }
        final Query query = setQueryParametersForOfflineStatus(abstractEstimateForLoaSearchRequest, queryStr);
        abstractEstimateList = query.getResultList();
        return abstractEstimateList;
    }

    private Query setQueryParametersForOfflineStatus(
            final AbstractEstimateForLoaSearchRequest abstractEstimateForLoaSearchRequest,
            final StringBuilder queryStr) {
        final Query qry = entityManager.createQuery(queryStr.toString());
        if (abstractEstimateForLoaSearchRequest != null) {
            if (abstractEstimateForLoaSearchRequest.getAbstractEstimateNumber() != null)
                qry.setParameter("abstractEstimateNumber",
                        abstractEstimateForLoaSearchRequest.getAbstractEstimateNumber().toUpperCase());
            if (abstractEstimateForLoaSearchRequest.getAdminSanctionFromDate() != null)
                qry.setParameter("abstractEstimateFromDate", abstractEstimateForLoaSearchRequest.getAdminSanctionFromDate());
            if (abstractEstimateForLoaSearchRequest.getAdminSanctionToDate() != null)
                qry.setParameter("abstractEstimateToDate", abstractEstimateForLoaSearchRequest.getAdminSanctionToDate());
            if (abstractEstimateForLoaSearchRequest.getAbstractEstimateCreatedBy() != null)
                qry.setParameter("abstractEstimateCreatedBy", abstractEstimateForLoaSearchRequest.getAbstractEstimateCreatedBy());

            if (abstractEstimateForLoaSearchRequest.getEgwStatus() != null) {
                qry.setParameter("objectType", WorksConstants.ABSTRACTESTIMATE);
                if (!abstractEstimateForLoaSearchRequest.getEgwStatus().equals(WorksConstants.ADMIN_SANCTIONED_STATUS))
                    qry.setParameter("offlineStatus",
                            abstractEstimateForLoaSearchRequest.getEgwStatus().toString().toLowerCase());
            }
            qry.setParameter("abstractEstimateStatus", WorksConstants.ADMIN_SANCTIONED_STATUS);
            qry.setParameter("workOrderStatus", WorksConstants.CANCELLED_STATUS);

        }
        return qry;
    }

    public List<String> getAbstractEstimateNumbersToSetOfflineStatus(final String code) {
        final List<String> estimateNumbers = abstractEstimateRepository
                .findAbstractEstimateNumbersToSetOfflineStatus("%" + code + "%",
                        WorksConstants.ADMIN_SANCTIONED_STATUS, WorksConstants.CANCELLED_STATUS);
        return estimateNumbers;
    }

    public void validateLocationDetails(final AbstractEstimate abstractEstimate, final BindingResult bindErrors) {
        if (worksApplicationProperties.locationDetailsRequired().toString().equalsIgnoreCase("Yes")) {
            final List<AppConfigValues> appConfigvalues = appConfigValuesService.getConfigValuesByModuleAndKey(
                    WorksConstants.WORKS_MODULE_NAME, WorksConstants.APPCONFIG_KEY_GIS_INTEGRATION);
            final AppConfigValues value = appConfigvalues.get(0);
            if (value.getValue().equalsIgnoreCase("Yes")
                    && (StringUtils.isBlank(abstractEstimate.getLocation()) || abstractEstimate.getLatitude() == null ||
                            abstractEstimate.getLongitude() == null))
                bindErrors.reject("error.locationdetails.required", "error.locationdetails.required");
        }
    }

    public void loadLocationAppConfigValue(final Model model) {
        final List<AppConfigValues> locationAppConfigvalues = appConfigValuesService.getConfigValuesByModuleAndKey(
                WorksConstants.WORKS_MODULE_NAME, WorksConstants.APPCONFIG_KEY_GIS_INTEGRATION);
        final AppConfigValues locationAppConfigValue = locationAppConfigvalues.get(0);
        if (locationAppConfigValue.getValue().equalsIgnoreCase("Yes"))
            model.addAttribute("isLocationDetailsRequired", true);
        else
            model.addAttribute("isLocationDetailsRequired", false);
    }

    public List<String> getApprovedEstimateNumbersForCreateLOA(final String estimateNumber) {
        final List<String> estimateNumbers = abstractEstimateRepository
                .findEstimateNumbersToCreateLOA("%" + estimateNumber + "%",
                        EstimateStatus.ADMIN_SANCTIONED.toString(), WorksConstants.CANCELLED_STATUS,
                        WorksConstants.ABSTRACTESTIMATE,
                        OfflineStatusesForAbstractEstimate.L1_TENDER_FINALIZED.toString());
        return estimateNumbers;
    }

    public List<String> getApprovedAdminSanctionNumbersForCreateLOA(final String adminSanctionNumber) {
        final List<String> adminSanctionNumbers = abstractEstimateRepository
                .findAdminSanctionNumbersToCreateLOA("%" + adminSanctionNumber + "%",
                        EstimateStatus.ADMIN_SANCTIONED.toString(), WorksConstants.CANCELLED_STATUS,
                        WorksConstants.ABSTRACTESTIMATE,
                        OfflineStatusesForAbstractEstimate.L1_TENDER_FINALIZED.toString());
        return adminSanctionNumbers;
    }

    public List<String> getApprovedWorkIdentificationNumbersForCreateLOA(final String workIdentificationNumber) {
        final List<String> workIdentificationNumbers = abstractEstimateRepository
                .findWorkIdentificationNumbersToCreateLOA("%" + workIdentificationNumber + "%",
                        EstimateStatus.ADMIN_SANCTIONED.toString(), WorksConstants.CANCELLED_STATUS,
                        WorksConstants.ABSTRACTESTIMATE,
                        OfflineStatusesForAbstractEstimate.L1_TENDER_FINALIZED.toString());
        return workIdentificationNumbers;
    }

    public List<Hashtable<String, Object>> getMeasurementSheetForEstimate(final AbstractEstimate abstractEstimate) {
        final List<Hashtable<String, Object>> measurementSheetList = new ArrayList<Hashtable<String, Object>>();
        int slno = 1;
        Hashtable<String, Object> measurementSheetMap = null;
        final List<String> characters = new ArrayList<String>(26);
        for (char c = 'a'; c <= 'z'; c++)
            characters.add(String.valueOf(c));
        for (final Activity activity : abstractEstimate.getActivities())
            if (activity.getMeasurementSheetList().size() != 0) {
                measurementSheetList.add(addActivityName(activity, slno));
                int measurementSNo = 1;
                for (final MeasurementSheet ms : activity.getMeasurementSheetList()) {
                    measurementSheetMap = new Hashtable<String, Object>(0);
                    measurementSheetMap.put("sNo", String.valueOf(slno) + characters.get((measurementSNo - 1) % 26));
                    measurementSheetMap.put("no", ms.getNo() != null ? ms.getNo() : "");
                    measurementSheetMap.put("scheduleCategory", "");
                    measurementSheetMap.put("scheduleCode", "");
                    measurementSheetMap.put("description", ms.getRemarks() != null ? ms.getRemarks() : "");
                    measurementSheetMap.put("length", ms.getLength() != null ? ms.getLength() : "");
                    measurementSheetMap.put("width", ms.getWidth() != null ? ms.getWidth() : "");
                    measurementSheetMap.put("depthHeigth", ms.getDepthOrHeight() != null ? ms.getDepthOrHeight() : "");
                    if (ms.getIdentifier() == 'D')
                        measurementSheetMap.put("quantity", BigDecimal.ZERO.subtract(ms.getQuantity()));
                    else
                        measurementSheetMap.put("quantity", ms.getQuantity());
                    measurementSheetMap.put("rate", "");
                    measurementSheetMap.put("uom", "");
                    measurementSheetMap.put("amount", "");
                    measurementSheetList.add(measurementSheetMap);
                    measurementSNo++;
                }
                if (activity.getMeasurementSheetList().size() != 0) {
                    measurementSheetMap = new Hashtable<String, Object>(0);
                    measurementSheetMap.put("sNo", "");
                    measurementSheetMap.put("no", "");
                    measurementSheetMap.put("scheduleCategory", "");
                    measurementSheetMap.put("scheduleCode", "");
                    measurementSheetMap.put("description", "");
                    measurementSheetMap.put("length", "");
                    measurementSheetMap.put("width", "");
                    measurementSheetMap.put("depthHeigth", "");
                    measurementSheetMap.put("quantity", activity.getQuantity());
                    measurementSheetMap.put("rate", activity.getRate());
                    measurementSheetMap.put("uom", activity.getUom().getUom());
                    measurementSheetMap.put("amount", activity.getAmount().getValue());
                    measurementSheetList.add(measurementSheetMap);
                }
                slno++;
            } else
                measurementSheetList.add(addActivityName(activity, slno++));

        return measurementSheetList;
    }

    private Hashtable<String, Object> addActivityName(final Activity activity, final int slNo) {
        final Hashtable<String, Object> measurementSheetMap = new Hashtable<String, Object>(0);
        measurementSheetMap.put("sNo", slNo);
        measurementSheetMap.put("no", "");
        if (activity.getSchedule() != null) {
            measurementSheetMap.put("scheduleCategory", activity.getSchedule().getScheduleCategory().getCode());
            measurementSheetMap.put("scheduleCode", activity.getSchedule().getCode());
            measurementSheetMap.put("description", activity.getSchedule().getDescription());
        } else {
            measurementSheetMap.put("scheduleCategory", "N/A");
            measurementSheetMap.put("scheduleCode", "N/A");
            measurementSheetMap.put("description", activity.getNonSor().getDescription());
        }
        measurementSheetMap.put("length", "");
        measurementSheetMap.put("width", "");
        measurementSheetMap.put("depthHeigth", "");
        if (activity.getMeasurementSheetList().size() != 0) {
            measurementSheetMap.put("quantity", "");
            measurementSheetMap.put("rate", "");
            measurementSheetMap.put("uom", "");
            measurementSheetMap.put("amount", "");
        } else {
            measurementSheetMap.put("quantity", activity.getQuantity());
            measurementSheetMap.put("rate", activity.getEstimateRate());
            measurementSheetMap.put("uom", activity.getUom().getUom());
            measurementSheetMap.put("amount", activity.getAmount().getValue());
        }
        return measurementSheetMap;
    }

    public List<AbstractEstimate> getBySorIdAndEstimateDate(final Long sorId, final Date estimateDate) {
        return abstractEstimateRepository.findBySorIdAndEstimateDate(sorId, estimateDate, WorksConstants.CANCELLED_STATUS);
    }

    public List<Activity> getActivitiesByParent(final Long activityId) {
        return abstractEstimateRepository.findActivitiesByParent(activityId, RevisionEstimateStatus.APPROVED.toString());
    }

}
