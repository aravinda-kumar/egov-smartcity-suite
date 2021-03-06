/*
 *    eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) 2018  eGovernments Foundation
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
 *            Further, all user interfaces, including but not limited to citizen facing interfaces,
 *            Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *            derived works should carry eGovernments Foundation logo on the top right corner.
 *
 *            For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 *            For any further queries on attribution, including queries on brand guidelines,
 *            please contact contact@egovernments.org
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
 *
 */
package org.egov.wtms.web.controller.application;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.egov.wtms.masters.entity.enums.ConnectionType.NON_METERED;
import static org.egov.wtms.utils.constants.WaterTaxConstants.ADDNLCONNECTION;
import static org.egov.wtms.utils.constants.WaterTaxConstants.APPLICATION_NUMBER;
import static org.egov.wtms.utils.constants.WaterTaxConstants.APPLICATION_STATUS_CLOSERDIGSIGNPENDING;
import static org.egov.wtms.utils.constants.WaterTaxConstants.APPLICATION_STATUS_CLOSERINITIATED;
import static org.egov.wtms.utils.constants.WaterTaxConstants.APPLICATION_STATUS_CLOSERINPROGRESS;
import static org.egov.wtms.utils.constants.WaterTaxConstants.APPLICATION_STATUS_CREATED;
import static org.egov.wtms.utils.constants.WaterTaxConstants.APPLICATION_STATUS_DIGITALSIGNPENDING;
import static org.egov.wtms.utils.constants.WaterTaxConstants.APPLICATION_STATUS_ESTIMATENOTICEGEN;
import static org.egov.wtms.utils.constants.WaterTaxConstants.APPLICATION_STATUS_FEEPAID;
import static org.egov.wtms.utils.constants.WaterTaxConstants.APPLICATION_STATUS_RECONNCTIONINPROGRESS;
import static org.egov.wtms.utils.constants.WaterTaxConstants.APPLICATION_STATUS_RECONNDIGSIGNPENDING;
import static org.egov.wtms.utils.constants.WaterTaxConstants.APPLICATION_STATUS_WOGENERATED;
import static org.egov.wtms.utils.constants.WaterTaxConstants.APPROVAL_COMMENT;
import static org.egov.wtms.utils.constants.WaterTaxConstants.APPROVAL_POSITION;
import static org.egov.wtms.utils.constants.WaterTaxConstants.APPROVED;
import static org.egov.wtms.utils.constants.WaterTaxConstants.APPROVEWORKFLOWACTION;
import static org.egov.wtms.utils.constants.WaterTaxConstants.ASSISTANT_ENGINEER_DESIGN;
import static org.egov.wtms.utils.constants.WaterTaxConstants.CHANGEOFUSE;
import static org.egov.wtms.utils.constants.WaterTaxConstants.CLOSECONNECTION;
import static org.egov.wtms.utils.constants.WaterTaxConstants.CLOSINGCONNECTION;
import static org.egov.wtms.utils.constants.WaterTaxConstants.COMMISSIONER_DESGN;
import static org.egov.wtms.utils.constants.WaterTaxConstants.DEPUTY_ENGINEER_DESIGN;
import static org.egov.wtms.utils.constants.WaterTaxConstants.EXECUTIVE_ENGINEER_DESIGN;
import static org.egov.wtms.utils.constants.WaterTaxConstants.FILESTORE_MODULECODE;
import static org.egov.wtms.utils.constants.WaterTaxConstants.FILE_STORE_ID_APPLICATION_NUMBER;
import static org.egov.wtms.utils.constants.WaterTaxConstants.FORWARDWORKFLOWACTION;
import static org.egov.wtms.utils.constants.WaterTaxConstants.JUNIOR_ASSISTANT_DESIGN;
import static org.egov.wtms.utils.constants.WaterTaxConstants.JUNIOR_OR_SENIOR_ASSISTANT_DESIGN;
import static org.egov.wtms.utils.constants.WaterTaxConstants.MODULETYPE;
import static org.egov.wtms.utils.constants.WaterTaxConstants.MODULE_NAME;
import static org.egov.wtms.utils.constants.WaterTaxConstants.MUNICIPAL_ENGINEER_DESIGN;
import static org.egov.wtms.utils.constants.WaterTaxConstants.NEWCONNECTION;
import static org.egov.wtms.utils.constants.WaterTaxConstants.COMM_APPROVAL_PENDING;
import static org.egov.wtms.utils.constants.WaterTaxConstants.PENDING_DIGI_SIGN;
import static org.egov.wtms.utils.constants.WaterTaxConstants.PERMENENTCLOSECODE;
import static org.egov.wtms.utils.constants.WaterTaxConstants.PROCEED_WITHOUT_METER_EST_AMT;
import static org.egov.wtms.utils.constants.WaterTaxConstants.PROCEED_WITHOUT_NONMETER_EST_AMT;
import static org.egov.wtms.utils.constants.WaterTaxConstants.RECONNECTION;
import static org.egov.wtms.utils.constants.WaterTaxConstants.REGULARIZE_CONNECTION;
import static org.egov.wtms.utils.constants.WaterTaxConstants.SENIOR_ASSISTANT_DESIGN;
import static org.egov.wtms.utils.constants.WaterTaxConstants.SIGNED_DOCUMENT_PREFIX;
import static org.egov.wtms.utils.constants.WaterTaxConstants.SIGNWORKFLOWACTION;
import static org.egov.wtms.utils.constants.WaterTaxConstants.SUBMITWORKFLOWACTION;
import static org.egov.wtms.utils.constants.WaterTaxConstants.SUPERIENTEND_ENGINEER_DESIGN;
import static org.egov.wtms.utils.constants.WaterTaxConstants.SUPERINTENDING_ENGINEER_DESIGNATION;
import static org.egov.wtms.utils.constants.WaterTaxConstants.WCMS_PENALTY_CHARGES_PERCENTAGE;
import static org.egov.wtms.utils.constants.WaterTaxConstants.WFLOW_ACTION_STEP_REJECT;
import static org.egov.wtms.utils.constants.WaterTaxConstants.WF_PREVIEW_BUTTON;
import static org.egov.wtms.utils.constants.WaterTaxConstants.WF_RECONNECTIONACKNOWLDGEENT_BUTTON;
import static org.egov.wtms.utils.constants.WaterTaxConstants.WF_STATE_APPROVAL_PENDING;
import static org.egov.wtms.utils.constants.WaterTaxConstants.WF_STATE_BUTTON_GENERATEESTIMATE;
import static org.egov.wtms.utils.constants.WaterTaxConstants.WF_STATE_CLERK_APPROVED;
import static org.egov.wtms.utils.constants.WaterTaxConstants.WF_STATE_REJECTED;
import static org.egov.wtms.utils.constants.WaterTaxConstants.WORKFLOW_ACTION;
import static org.egov.wtms.utils.constants.WaterTaxConstants.WORKFLOW_RECONNCTIONINITIATED;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.ValidationException;

import org.egov.commons.entity.ChairPerson;
import org.egov.commons.service.ChairPersonService;
import org.egov.eis.entity.Assignment;
import org.egov.eis.web.contract.WorkflowContainer;
import org.egov.infra.admin.master.entity.AppConfig;
import org.egov.infra.admin.master.service.AppConfigService;
import org.egov.infra.admin.master.service.CityService;
import org.egov.infra.admin.master.service.UserService;
import org.egov.infra.config.core.ApplicationThreadLocals;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.reporting.engine.ReportOutput;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.utils.DateUtils;
import org.egov.infra.utils.autonumber.AutonumberServiceBeanResolver;
import org.egov.wtms.application.entity.ApplicationDocuments;
import org.egov.wtms.application.entity.WaterConnectionDetails;
import org.egov.wtms.application.service.ConnectionDemandService;
import org.egov.wtms.application.service.ReportGenerationService;
import org.egov.wtms.autonumber.EstimationNumberGenerator;
import org.egov.wtms.autonumber.WorkOrderNumberGenerator;
import org.egov.wtms.masters.entity.ConnectionCategory;
import org.egov.wtms.masters.entity.DonationDetails;
import org.egov.wtms.masters.entity.enums.ClosureType;
import org.egov.wtms.masters.service.MeterCostService;
import org.egov.wtms.masters.service.RoadCategoryService;
import org.egov.wtms.utils.WaterTaxNumberGenerator;
import org.egov.wtms.utils.WaterTaxUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value = "/application")
public class UpdateConnectionController extends GenericConnectionController {

    private static final String PROCEEDWITHOUTDONATION = "Proceed Without Donation";
    private static final String PENALTY_AMOUNT = "penaltyAmount";
    private static final String SAVE = "Save";
    private static final String ADD_DEMAND = "addDemand";
    private static final String EDIT_DEMAND = "editDemand";
    private static final String ADDITIONALRULE = "additionalRule";
    private static final String APPRIVALPOSITION = "approvalPosition";
    private static final String FILESTOREIDS = "fileStoreIds";
    private static final String APPROVALPOSITIONEXIST = "approvalPositionExist";
    private static final String FIELDINSPECTION = "fieldInspection";
    private static final String MODE = "mode";
    private static final String APP_DOCUMENT_LIST = "appforDocumentList";
    private static final String DONATION_AMOUNT = "donationCharges";
    private static final String ROAD_CATEGORY_LIST = "roadCategoryList";
    private static final String REJECTED = "Rejected";
    private static final String YES = "Yes";

    @Autowired
    private ConnectionDemandService connectionDemandService;

    @Autowired
    private WaterTaxUtils waterTaxUtils;

    @Autowired
    private RoadCategoryService roadCategoryService;

    @Autowired
    private AutonumberServiceBeanResolver beanResolver;

    @Autowired
    private SecurityUtils securityUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private ChairPersonService chairPersonService;

    @Autowired
    private MeterCostService meterCostService;

    @Autowired
    private ReportGenerationService reportGenerationService;

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private CityService cityService;

    @Autowired
    private WaterTaxNumberGenerator waterTaxNumberGenerator;

    @ModelAttribute
    public WaterConnectionDetails getWaterConnectionDetails(@PathVariable final String applicationNumber) {
        return waterConnectionDetailsService.findByApplicationNumber(applicationNumber);
    }

    @RequestMapping(value = "/update/{applicationNumber}", method = RequestMethod.GET)
    public String view(final Model model, @PathVariable final String applicationNumber,
            final HttpServletRequest request) {
        final WaterConnectionDetails waterConnectionDetails = waterConnectionDetailsService
                .findByApplicationNumber(applicationNumber);
        return loadViewData(model, request, waterConnectionDetails);
    }

    private String loadViewData(final Model model, final HttpServletRequest request,
            final WaterConnectionDetails waterConnectionDetails) {
        model.addAttribute("stateType", waterConnectionDetails.getClass().getSimpleName());
        final WorkflowContainer workflowContainer = new WorkflowContainer();
        Boolean isCommissionerLoggedIn = Boolean.FALSE;
        Boolean isSanctionedDetailEnable = isCommissionerLoggedIn;
        final String loggedInUserDesignation = waterTaxUtils.loggedInUserDesignation(waterConnectionDetails);

        if (REGULARIZE_CONNECTION.equalsIgnoreCase(waterConnectionDetails.getApplicationType().getCode()) &&
                (APPLICATION_STATUS_CREATED.equalsIgnoreCase(waterConnectionDetails.getStatus().getCode()) ||
                        APPLICATION_STATUS_ESTIMATENOTICEGEN.equalsIgnoreCase(waterConnectionDetails.getStatus().getCode()))) {
            final DonationDetails donationDetails = connectionDemandService.getDonationDetails(waterConnectionDetails);
            final Double donationAmount = donationDetails == null ? 0d
                    : connectionDemandService.getDonationDetails(waterConnectionDetails).getAmount();
            BigDecimal penaltyPercent = BigDecimal.ZERO;
            final AppConfig appConfig = appConfigService.getAppConfigByModuleNameAndKeyName(MODULE_NAME,
                    WCMS_PENALTY_CHARGES_PERCENTAGE);
            if (appConfig != null && !appConfig.getConfValues().isEmpty())
                penaltyPercent = BigDecimal.valueOf(Long.valueOf(appConfig.getConfValues().get(0).getValue()));
            model.addAttribute(DONATION_AMOUNT, donationAmount.longValue());
            model.addAttribute(PENALTY_AMOUNT,
                    BigDecimal.valueOf(donationAmount).multiply(penaltyPercent).divide(new BigDecimal(100)));
            model.addAttribute("currentDemand", waterTaxUtils.getCurrentDemand(waterConnectionDetails).getDemand());
        }

        if (waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_FEEPAID)
                || waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_DIGITALSIGNPENDING)
                || waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_CLOSERDIGSIGNPENDING)
                || waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_RECONNDIGSIGNPENDING)
                || (waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_RECONNCTIONINPROGRESS)
                        || waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_CLOSERINPROGRESS))
                        && !waterConnectionDetails.getState().getValue().equals(WF_STATE_REJECTED))
            workflowContainer.setCurrentDesignation(loggedInUserDesignation);
        if (loggedInUserDesignation != null
                && (loggedInUserDesignation.equalsIgnoreCase(SENIOR_ASSISTANT_DESIGN)
                        || loggedInUserDesignation.equalsIgnoreCase(JUNIOR_ASSISTANT_DESIGN))
                && waterConnectionDetails.getStatus() != null
                && (waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_CREATED)
                        || waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_CLOSERINITIATED)
                        || waterConnectionDetails.getStatus().getCode().equals(WORKFLOW_RECONNCTIONINITIATED))
                || loggedInUserDesignation.equalsIgnoreCase(ASSISTANT_ENGINEER_DESIGN)
                        && (waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_CLOSERINITIATED)
                                || waterConnectionDetails.getStatus().getCode().equals(WORKFLOW_RECONNCTIONINITIATED)))
            workflowContainer.setCurrentDesignation(null);
        if (waterConnectionDetails.getCloseConnectionType() != null
                && waterConnectionDetails.getReConnectionReason() == null) {
            model.addAttribute(ADDITIONALRULE, CLOSECONNECTION);
            workflowContainer.setAdditionalRule(CLOSECONNECTION);
            if (waterConnectionDetails.getCloseConnectionType().equals(PERMENENTCLOSECODE))
                waterConnectionDetails.setCloseConnectionType(ClosureType.Permanent.getName());
            else
                waterConnectionDetails.setCloseConnectionType(ClosureType.Temporary.getName());

            model.addAttribute("radioButtonMap", Arrays.asList(ClosureType.values()));
        }
        model.addAttribute("applicationDocList",
                waterConnectionDetailsService.getApplicationDocForExceptClosureAndReConnection(waterConnectionDetails));
        if (waterConnectionDetails.getCloseConnectionType() != null
                && (waterConnectionDetails.getReConnectionReason() == null
                        || waterConnectionDetails.getReConnectionReason() != null))
            if (!waterConnectionDetails.getApplicationDocs().isEmpty())
                for (final ApplicationDocuments appDoc : waterConnectionDetails.getApplicationDocs()) {
                    if (appDoc.getDocumentNames() != null
                            && appDoc.getDocumentNames().getApplicationType().getCode().equals(CLOSINGCONNECTION)) {
                        final List<ApplicationDocuments> tempListDoc = new ArrayList<>();
                        tempListDoc.add(appDoc);
                        model.addAttribute(APP_DOCUMENT_LIST, tempListDoc);
                    }
                    if (appDoc.getDocumentNames() != null && appDoc.getDocumentNames().getApplicationType().getCode()
                            .equals(RECONNECTION)) {
                        final List<ApplicationDocuments> tempListDocrecon = new ArrayList<>();
                        tempListDocrecon.add(appDoc);
                        model.addAttribute(APP_DOCUMENT_LIST, tempListDocrecon);
                    }

                }
            else
                model.addAttribute(APP_DOCUMENT_LIST, waterConnectionDetails.getApplicationDocs());

        if (waterConnectionDetails.getCloseConnectionType() != null
                && waterConnectionDetails.getReConnectionReason() != null) {
            model.addAttribute(ADDITIONALRULE, RECONNECTION);
            workflowContainer.setAdditionalRule(RECONNECTION);

        } else if (isBlank(workflowContainer.getAdditionalRule())) {
            workflowContainer.setAdditionalRule(waterConnectionDetails.getApplicationType().getCode());
            model.addAttribute(ADDITIONALRULE, waterConnectionDetails.getApplicationType().getCode());
        }
        if (WF_STATE_APPROVAL_PENDING.equalsIgnoreCase(waterConnectionDetails.getCurrentState().getValue()) &&
                waterConnectionDetails.getApprovalNumber() != null && COMMISSIONER_DESGN.equalsIgnoreCase(loggedInUserDesignation)
                && APPLICATION_STATUS_DIGITALSIGNPENDING.equalsIgnoreCase(waterConnectionDetails.getStatus().getCode()))
            workflowContainer.setPendingActions(PENDING_DIGI_SIGN);
        else if (Arrays.asList(NEWCONNECTION, ADDNLCONNECTION, CHANGEOFUSE)
                .contains(waterConnectionDetails.getApplicationType().getCode()) &&
                APPLICATION_STATUS_FEEPAID.equalsIgnoreCase(waterConnectionDetails.getStatus().getCode()) &&
                COMMISSIONER_DESGN.equalsIgnoreCase(loggedInUserDesignation))
            workflowContainer.setPendingActions(COMM_APPROVAL_PENDING);
        prepareWorkflow(model, waterConnectionDetails, workflowContainer);
        model.addAttribute("currentState", waterConnectionDetails.getCurrentState().getValue());
        model.addAttribute("currentUser", waterTaxUtils.getCurrentUserRole(securityUtils.getCurrentUser()));
        model.addAttribute("waterConnectionDetails", waterConnectionDetails);
        model.addAttribute("feeDetails", connectionDemandService.getSplitFee(waterConnectionDetails));
        model.addAttribute("connectionType", waterConnectionDetailsService.getConnectionTypesMap()
                .get(waterConnectionDetails.getConnectionType().name()));
        model.addAttribute("applicationHistory", waterConnectionDetailsService.getHistory(waterConnectionDetails));
        model.addAttribute("approvalDepartmentList", departmentService.getAllDepartments());
        if (waterConnectionDetails.getStatus() != null
                && waterConnectionDetails.getStatus().getCode().equalsIgnoreCase(APPLICATION_STATUS_WOGENERATED))
            model.addAttribute("meterCostMasters",
                    meterCostService.findByPipeSize(waterConnectionDetails.getPipeSize()));

        if (waterConnectionDetails.getStatus() != null
                && waterConnectionDetails.getStatus().getCode().equalsIgnoreCase(APPLICATION_STATUS_FEEPAID)) {
            final ChairPerson chairPerson = chairPersonService.getActiveChairPersonAsOnCurrentDate();
            model.addAttribute("chairPerson", chairPerson);
            model.addAttribute("sanctionDateLowerLimit",
                    DateUtils.daysBetween(waterConnectionDetails.getApplicationDate(), new Date()));

        }

        appendModeBasedOnApplicationCreator(model, request, waterConnectionDetails);
        if (loggedInUserDesignation != null && !"".equals(loggedInUserDesignation)
                && loggedInUserDesignation.equalsIgnoreCase(COMMISSIONER_DESGN)
                && (waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_DIGITALSIGNPENDING)
                        || waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_FEEPAID)
                        || waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_CLOSERDIGSIGNPENDING)
                        || waterConnectionDetails.getStatus().getCode()
                                .equals(APPLICATION_STATUS_RECONNCTIONINPROGRESS)
                        || waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_CLOSERINPROGRESS)
                        || waterConnectionDetails.getStatus().getCode()
                                .equals(APPLICATION_STATUS_RECONNDIGSIGNPENDING)))
            isCommissionerLoggedIn = Boolean.TRUE;
        if (COMMISSIONER_DESGN.equalsIgnoreCase(loggedInUserDesignation)
                || DEPUTY_ENGINEER_DESIGN.equalsIgnoreCase(loggedInUserDesignation)
                || EXECUTIVE_ENGINEER_DESIGN.equalsIgnoreCase(loggedInUserDesignation)
                || MUNICIPAL_ENGINEER_DESIGN.equalsIgnoreCase(loggedInUserDesignation)
                || SUPERIENTEND_ENGINEER_DESIGN.equalsIgnoreCase(loggedInUserDesignation)
                || SUPERINTENDING_ENGINEER_DESIGNATION.equalsIgnoreCase(loggedInUserDesignation)
                        && (waterConnectionDetails.getApprovalNumber() == null ||
                                !APPLICATION_STATUS_DIGITALSIGNPENDING
                                        .equalsIgnoreCase(waterConnectionDetails.getStatus().getCode())))
            isSanctionedDetailEnable = Boolean.TRUE;

        final BigDecimal waterTaxDueforParent = waterConnectionDetailsService.getWaterTaxDueAmount(waterConnectionDetails);
        model.addAttribute("waterTaxDueforParent", waterTaxDueforParent);
        if (loggedInUserDesignation != null
                && (loggedInUserDesignation.equals(SENIOR_ASSISTANT_DESIGN)
                        || loggedInUserDesignation.equals(JUNIOR_ASSISTANT_DESIGN))
                && waterConnectionDetails.getStatus() != null
                && (waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_CREATED)
                        || waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_CLOSERINITIATED)
                        || waterConnectionDetails.getStatus().getCode().equals(WORKFLOW_RECONNCTIONINITIATED))
                || loggedInUserDesignation.equalsIgnoreCase(ASSISTANT_ENGINEER_DESIGN)
                        && (waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_CLOSERINITIATED)
                                || waterConnectionDetails.getStatus().getCode().equals(WORKFLOW_RECONNCTIONINITIATED)))
            model.addAttribute("currentDesignation", "");
        else
            model.addAttribute("currentDesignation", loggedInUserDesignation);

        if (!WF_STATE_REJECTED.equals(waterConnectionDetails.getState().getValue()) &&
                !WF_STATE_CLERK_APPROVED.equals(waterConnectionDetails.getState().getValue())) {
            final AppConfig appConfig = appConfigService.getAppConfigByModuleNameAndKeyName(MODULE_NAME,
                    NON_METERED.equals(waterConnectionDetails.getConnectionType()) ? PROCEED_WITHOUT_NONMETER_EST_AMT
                            : PROCEED_WITHOUT_METER_EST_AMT);
            if (YES.equalsIgnoreCase(appConfig.getConfValues().get(0).getValue()))
                model.addAttribute("proceedWithoutDonation", "true");
        }
        model.addAttribute("hasJuniorOrSeniorAssistantRole",
                waterTaxUtils.isLoggedInUserJuniorOrSeniorAssistant(ApplicationThreadLocals.getUserId()));
        model.addAttribute("reassignEnabled", waterTaxUtils.reassignEnabled());
        model.addAttribute("applicationState", waterConnectionDetails.getState().getValue());
        model.addAttribute("statuscode", waterConnectionDetails.getStatus().getCode());
        model.addAttribute("isCommissionerLoggedIn", isCommissionerLoggedIn);
        model.addAttribute("isSanctionedDetailEnable", isSanctionedDetailEnable);
        model.addAttribute("usageTypes", usageTypeService.getActiveUsageTypes());
        model.addAttribute("connectionCategories", connectionCategoryService.getAllActiveConnectionCategory());
        model.addAttribute("pipeSizes", pipeSizeService.getAllActivePipeSize());
        model.addAttribute("typeOfConnection", waterConnectionDetails.getApplicationType().getCode());
        model.addAttribute("ownerPosition", waterConnectionDetails.getState().getOwnerPosition().getId());
        return "newconnection-edit";
    }

    private void appendModeBasedOnApplicationCreator(final Model model, final HttpServletRequest request,
            final WaterConnectionDetails waterConnectionDetails) {
        final Boolean recordCreatedBYNonEmployee = waterTaxUtils
                .getCurrentUserRole(waterConnectionDetails.getCreatedBy());
        final Boolean recordCreatedBYCitizenPortal = waterTaxUtils
                .isCitizenPortalUser(userService.getUserById(waterConnectionDetails.getCreatedBy().getId()));
        final Boolean recordCreatedByAnonymousUser = waterTaxUtils
                .isAnonymousUser(userService.getUserById(waterConnectionDetails.getCreatedBy().getId()));

        if ((recordCreatedBYNonEmployee || recordCreatedBYCitizenPortal || recordCreatedByAnonymousUser)
                && null == request.getAttribute(MODE) && waterConnectionDetails.getState().getHistory().isEmpty()) {
            model.addAttribute(MODE, "edit");
            model.addAttribute(APPROVALPOSITIONEXIST,
                    waterConnectionDetailsService.getApprovalPositionByMatrixDesignation(waterConnectionDetails, 0l,
                            CLOSINGCONNECTION.equals(waterConnectionDetails.getApplicationType().getCode())
                                    ? CLOSECONNECTION : waterConnectionDetails.getApplicationType().getCode(),
                            "edit", ""));
        }
        // "edit" mode for AE inbox record FROM CSC and Record from Clerk
        else if (!REGULARIZE_CONNECTION.equalsIgnoreCase(waterConnectionDetails.getApplicationType().getCode()) &&
                (recordCreatedBYNonEmployee || recordCreatedBYCitizenPortal)
                && request.getAttribute(MODE) == null
                && waterConnectionDetails.getState().getHistory() != null && waterConnectionDetails.getStatus() != null) {
            String additionalRule = "";
            if (waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_CLOSERINITIATED))
                additionalRule = CLOSECONNECTION;
            else if (waterConnectionDetails.getStatus().getCode().equals(WORKFLOW_RECONNCTIONINITIATED))
                additionalRule = RECONNECTION;
            else if (!CLOSINGCONNECTION.equalsIgnoreCase(waterConnectionDetails.getApplicationType().getCode()))
                additionalRule = waterConnectionDetails.getApplicationType().getCode();
            model.addAttribute(MODE, FIELDINSPECTION);
            model.addAttribute(APPROVALPOSITIONEXIST,
                    waterConnectionDetailsService.getApprovalPositionByMatrixDesignation(waterConnectionDetails, 0l,
                            additionalRule, FIELDINSPECTION, ""));
            model.addAttribute(ROAD_CATEGORY_LIST, roadCategoryService.getAllRoadCategory());

        } else if (REGULARIZE_CONNECTION.equalsIgnoreCase(waterConnectionDetails.getApplicationType().getCode()) &&
                (APPLICATION_STATUS_CREATED.equalsIgnoreCase(waterConnectionDetails.getStatus().getCode()) ||
                        APPLICATION_STATUS_ESTIMATENOTICEGEN.equalsIgnoreCase(waterConnectionDetails.getStatus().getCode()))
                &&
                waterTaxUtils.getCurrentDemand(waterConnectionDetails).getDemand() != null) {
            final boolean isWaterChargeDemandPresent = connectionDemandService
                    .checkWaterChargesCurrentDemand(waterConnectionDetails);
            if (isWaterChargeDemandPresent)
                model.addAttribute(MODE, EDIT_DEMAND);
            else
                model.addAttribute(MODE, ADD_DEMAND);
            model.addAttribute(ROAD_CATEGORY_LIST, roadCategoryService.getAllRoadCategory());
        } else if (REGULARIZE_CONNECTION.equalsIgnoreCase(waterConnectionDetails.getApplicationType().getCode()) &&
                waterConnectionDetails.getExecutionDate() == null && recordCreatedBYNonEmployee) {
            model.addAttribute(MODE, FIELDINSPECTION);
            model.addAttribute(ROAD_CATEGORY_LIST, roadCategoryService.getAllRoadCategory());
        } else if (!recordCreatedBYNonEmployee && waterConnectionDetails.getStatus() != null
                && waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_CREATED)) {
            model.addAttribute(MODE, FIELDINSPECTION);
            model.addAttribute(APPROVALPOSITIONEXIST,
                    waterConnectionDetailsService.getApprovalPositionByMatrixDesignation(waterConnectionDetails, 0l,
                            waterConnectionDetails.getApplicationType().getCode(), FIELDINSPECTION, ""));
            model.addAttribute(ROAD_CATEGORY_LIST, roadCategoryService.getAllRoadCategory());

        } else if (waterConnectionDetails.getCloseConnectionType() != null
                && waterConnectionDetails.getReConnectionReason() == null)
            model.addAttribute(APPROVALPOSITIONEXIST, waterConnectionDetailsService
                    .getApprovalPositionByMatrixDesignation(waterConnectionDetails, 0l, CLOSECONNECTION, "", ""));

        else if (waterConnectionDetails.getCloseConnectionType() != null
                && waterConnectionDetails.getReConnectionReason() != null)
            model.addAttribute(APPROVALPOSITIONEXIST,
                    waterConnectionDetailsService.getApprovalPositionByMatrixDesignation(waterConnectionDetails, 0l,
                            RECONNECTION, "", ""));
        else
            model.addAttribute(APPROVALPOSITIONEXIST,
                    waterConnectionDetailsService.getApprovalPositionByMatrixDesignation(waterConnectionDetails, 0l,
                            waterConnectionDetails.getApplicationType().getCode(), "", ""));
        if (waterConnectionDetails.getCurrentState().getValue().equals(REJECTED))
            model.addAttribute(MODE, "");

        if (waterConnectionDetails.getCloseConnectionType() != null
                && waterConnectionDetails.getReConnectionReason() == null
                && waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_CLOSERINITIATED))
            model.addAttribute(MODE, "closereditForAE");
        if ((waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_CLOSERINPROGRESS)
                || waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_CLOSERINITIATED))
                && waterConnectionDetails.getReConnectionReason() == null
                && waterConnectionDetails.getCloseConnectionType() != null
                && waterConnectionDetails.getState().getValue().equals(REJECTED))
            model.addAttribute(MODE, "closeredit");
        if (waterConnectionDetails.getReConnectionReason() != null
                && waterConnectionDetails.getState().getValue().equals(REJECTED)
                && waterConnectionDetails.getStatus().getCode().equals(WORKFLOW_RECONNCTIONINITIATED))
            model.addAttribute(MODE, "reconnectioneredit");
        if (waterConnectionDetails.getReConnectionReason() != null
                && waterConnectionDetails.getStatus().getCode().equals(WORKFLOW_RECONNCTIONINITIATED))
            model.addAttribute(MODE, "reconEditForAE");
    }

    @RequestMapping(value = "/update/{applicationNumber}", method = RequestMethod.POST)
    public String update(@Valid @ModelAttribute WaterConnectionDetails waterConnectionDetails,
            final BindingResult resultBinder, final RedirectAttributes redirectAttributes,
            final HttpServletRequest request, final Model model, @RequestParam("files") final MultipartFile[] files) {

        request.getParameter("currentState");
        request.getParameter("ownerPosition");

        String mode = "";
        String workFlowAction = "";
        Double donationCharges = 0d;
        final String sourceChannel = request.getParameter("Source");

        if (request.getParameter(WORKFLOW_ACTION) != null) {
            workFlowAction = request.getParameter(WORKFLOW_ACTION);
            request.getSession().setAttribute(WORKFLOW_ACTION, workFlowAction);
        }

        if (request.getParameter(DONATION_AMOUNT) != null)
            donationCharges = Double.valueOf(request.getParameter(DONATION_AMOUNT));
        if (request.getParameter(MODE) != null)
            mode = request.getParameter(MODE);

        if (PROCEEDWITHOUTDONATION.equalsIgnoreCase(workFlowAction)
                && APPLICATION_STATUS_ESTIMATENOTICEGEN.equalsIgnoreCase(waterConnectionDetails.getStatus().getCode()))
            waterConnectionDetails
                    .setStatus(waterTaxUtils.getStatusByCodeAndModuleType(APPLICATION_STATUS_FEEPAID, MODULETYPE));

        // For Submit Button
        if ((APPLICATION_STATUS_CREATED.equalsIgnoreCase(waterConnectionDetails.getStatus().getCode()) ||
                APPLICATION_STATUS_ESTIMATENOTICEGEN.equalsIgnoreCase(waterConnectionDetails.getStatus().getCode()))
                && (FIELDINSPECTION.equalsIgnoreCase(mode) || EDIT_DEMAND.equalsIgnoreCase(mode)))
            if (SUBMITWORKFLOWACTION.equalsIgnoreCase(workFlowAction) || FORWARDWORKFLOWACTION.equalsIgnoreCase(workFlowAction)
                    || SAVE.equalsIgnoreCase(workFlowAction)) {
                if (FIELDINSPECTION.equalsIgnoreCase(mode)) {
                    final ConnectionCategory connectionCategory = connectionCategoryService
                            .findOne(waterConnectionDetails.getCategory().getId());
                    connectionDemandService.generateDemandForApplication(waterConnectionDetails, connectionCategory,
                            donationCharges);
                    // Attach any other file during field inspection and estimation
                    final Set<FileStoreMapper> fileStoreSet = addToFileStore(files);
                    Iterator<FileStoreMapper> fsIterator = null;
                    if (fileStoreSet != null && !fileStoreSet.isEmpty())
                        fsIterator = fileStoreSet.iterator();
                    while (fsIterator != null && fsIterator.hasNext())
                        waterConnectionDetails.getFieldInspectionDetails().setFileStore(fsIterator.next());

                }
                if (REGULARIZE_CONNECTION.equalsIgnoreCase(waterConnectionDetails.getApplicationType().getCode()) &&
                        (APPLICATION_STATUS_CREATED.equalsIgnoreCase(waterConnectionDetails.getStatus().getCode()) ||
                                APPLICATION_STATUS_ESTIMATENOTICEGEN
                                        .equalsIgnoreCase(waterConnectionDetails.getStatus().getCode()))) {
                    if (!FIELDINSPECTION.equals(mode) &&
                            connectionDemandService.getWaterTaxDue(waterConnectionDetails, resultBinder).hasErrors())
                        return loadViewData(model, request, waterConnectionDetails);
                    if (waterConnectionDetails.getConnection().getConsumerCode() == null) {
                        connectionDemandService.createDemandDetailForPenaltyAndServiceCharges(waterConnectionDetails);
                        waterConnectionDetails.getConnection().setConsumerCode(waterTaxNumberGenerator.getNextConsumerNumber());
                    }

                    waterConnectionDetailsService.save(waterConnectionDetails);
                    return loadViewData(model, request, waterConnectionDetails);
                }

                waterConnectionDetailsService.save(waterConnectionDetails);
                waterConnectionDetailsService.getCurrentSession().flush();

            } else if (workFlowAction.equalsIgnoreCase(WFLOW_ACTION_STEP_REJECT)) {
                waterConnectionDetailsService.getCurrentSession().evict(waterConnectionDetails);
                waterConnectionDetails = waterConnectionDetailsService.findBy(waterConnectionDetails.getId());
            }

        Long approvalPosition = 0l;
        String approvalComent = "";

        if (request.getParameter("approvalComent") != null)
            approvalComent = request.getParameter("approvalComent");

        if (workFlowAction != null && workFlowAction.equals(APPROVEWORKFLOWACTION)
                && waterConnectionDetails.getStatus() != null && waterConnectionDetails.getStatus().getCode() != null
                && waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_FEEPAID))
            validateSanctionDetails(waterConnectionDetails, resultBinder);
        if (request.getParameter(APPRIVALPOSITION) != null && !request.getParameter(APPRIVALPOSITION).isEmpty())
            approvalPosition = Long.valueOf(request.getParameter(APPRIVALPOSITION));
        // For Get Configured ApprovalPosition from workflow history
        if (approvalPosition == null || approvalPosition.equals(Long.valueOf(0)))
            if (waterConnectionDetails.getCloseConnectionType() != null)
                approvalPosition = waterConnectionDetailsService.getApprovalPositionByMatrixDesignation(
                        waterConnectionDetails, approvalPosition, request.getParameter(ADDITIONALRULE), mode,
                        workFlowAction);
            else
                approvalPosition = waterConnectionDetailsService.getApprovalPositionByMatrixDesignation(
                        waterConnectionDetails, approvalPosition, waterConnectionDetails.getApplicationType().getCode(),
                        mode, workFlowAction);
        // to get modes to hide and show details in every user inbox

        request.getSession().setAttribute("APPROVAL_POSITION", approvalPosition);

        if (request.getParameter(APPRIVALPOSITION) == null)
            appendModeBasedOnApplicationCreator(model, request, waterConnectionDetails);

        if ((approvalPosition == null || approvalPosition.equals(Long.valueOf(0)))
                && request.getParameter(APPRIVALPOSITION) != null && !request.getParameter(APPRIVALPOSITION).isEmpty())
            approvalPosition = Long.valueOf(request.getParameter(APPRIVALPOSITION));
        // For ReConnection and Closure Connection
        if ((workFlowAction.equals(WFLOW_ACTION_STEP_REJECT)
                || workFlowAction.equalsIgnoreCase(WF_RECONNECTIONACKNOWLDGEENT_BUTTON))
                && (waterConnectionDetails.getStatus().getCode().equals(WORKFLOW_RECONNCTIONINITIATED)
                        || waterConnectionDetails.getStatus().getCode()
                                .equals(APPLICATION_STATUS_RECONNCTIONINPROGRESS)
                        || waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_CLOSERINPROGRESS)
                        || waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_CLOSERINITIATED)))
            approvalPosition = waterTaxUtils.getApproverPosition(JUNIOR_OR_SENIOR_ASSISTANT_DESIGN, waterConnectionDetails);
        if ((approvalPosition == null || approvalPosition.equals(Long.valueOf(0)))
                && (waterConnectionDetails.getStatus().getCode().equals(APPLICATION_STATUS_DIGITALSIGNPENDING)
                        || waterConnectionDetails.getStatus().getCode().equals(APPROVED)))
            throw new ValidationException("err.nouserdefinedforworkflow");
        if (!resultBinder.hasErrors()) {
            try {
                // For Closure Connection
                if (waterConnectionDetails.getCloseConnectionType() != null)
                    if (waterConnectionDetails.getCloseConnectionType().equals(PERMENENTCLOSECODE))
                        waterConnectionDetails.setCloseConnectionType(ClosureType.Permanent.getName());
                    else
                        waterConnectionDetails.setCloseConnectionType(ClosureType.Temporary.getName());

                if (workFlowAction != null)
                    if (APPROVEWORKFLOWACTION.equalsIgnoreCase(workFlowAction)) {

                        if (waterConnectionDetails.getConnection().getConsumerCode() == null)
                            waterConnectionDetails.getConnection()
                                    .setConsumerCode(waterTaxNumberGenerator.getNextConsumerNumber());

                        final WorkOrderNumberGenerator workOrderGen = beanResolver
                                .getAutoNumberServiceFor(WorkOrderNumberGenerator.class);
                        if (NEWCONNECTION.equals(waterConnectionDetails.getApplicationType().getCode())
                                || ADDNLCONNECTION.equals(waterConnectionDetails.getApplicationType().getCode())
                                || CHANGEOFUSE.equals(waterConnectionDetails.getApplicationType().getCode())
                                || REGULARIZE_CONNECTION.equals(waterConnectionDetails.getApplicationType().getCode())) {
                            waterConnectionDetails.setWorkOrderDate(new Date());
                            waterConnectionDetails.setWorkOrderNumber(workOrderGen.generateWorkOrderNumber());
                        }
                        ReportOutput reportOutput;
                        reportOutput = getReportOutputObject(waterConnectionDetails, workFlowAction);

                        // Setting FileStoreMap object while Commissioner Signs
                        // the document
                        if (reportOutput != null) {
                            String fileName;
                            if (CLOSINGCONNECTION.equals(waterConnectionDetails.getApplicationType().getCode()) ||
                                    RECONNECTION.equals(waterConnectionDetails.getApplicationType().getCode()) ||
                                    REGULARIZE_CONNECTION.equals(waterConnectionDetails.getApplicationType().getCode()))
                                fileName = SIGNED_DOCUMENT_PREFIX + waterConnectionDetails.getApplicationNumber()
                                        + ".pdf";
                            else
                                fileName = SIGNED_DOCUMENT_PREFIX + waterConnectionDetails.getWorkOrderNumber()
                                        + ".pdf";

                            final InputStream fileStream = new ByteArrayInputStream(reportOutput.getReportOutputData());
                            final FileStoreMapper fileStore = fileStoreService.store(fileStream, fileName,
                                    "application/pdf", FILESTORE_MODULECODE);
                            if (CLOSINGCONNECTION.equals(waterConnectionDetails.getApplicationType().getCode()))
                                waterConnectionDetails.setClosureFileStore(fileStore);
                            else if (RECONNECTION.equals(waterConnectionDetails.getApplicationType().getCode()))
                                waterConnectionDetails.setReconnectionFileStore(fileStore);
                            else
                                waterConnectionDetails.setFileStore(fileStore);
                            waterConnectionDetailsService
                                    .updateWaterConnectionDetailsWithFileStore(waterConnectionDetails);
                        }
                    } else if (workFlowAction.equalsIgnoreCase(WF_PREVIEW_BUTTON)
                            && (waterConnectionDetails.getApplicationType().getCode().equals(NEWCONNECTION)
                                    || waterConnectionDetails.getApplicationType().getCode().equals(ADDNLCONNECTION)
                                    || waterConnectionDetails.getApplicationType().getCode().equals(CHANGEOFUSE)))
                        return "redirect:/application/workorder?pathVar="
                                + waterConnectionDetails.getApplicationNumber();
                    else if (workFlowAction.equalsIgnoreCase(WF_PREVIEW_BUTTON)
                            && waterConnectionDetails.getApplicationType().getCode().equals(CLOSINGCONNECTION))
                        return "redirect:/application/acknowlgementNotice?pathVar="
                                + waterConnectionDetails.getApplicationNumber();
                    else if (workFlowAction.equalsIgnoreCase(WF_PREVIEW_BUTTON)
                            && waterConnectionDetails.getApplicationType().getCode().equals(RECONNECTION))
                        return "redirect:/application/ReconnacknowlgementNotice?pathVar="
                                + waterConnectionDetails.getApplicationNumber();
                    else if (workFlowAction.equals(SIGNWORKFLOWACTION)) { // Sign
                        final WaterConnectionDetails upadtedWaterConnectionDetails = waterConnectionDetailsService
                                .findByApplicationNumber(waterConnectionDetails.getApplicationNumber());

                        final HttpSession session = request.getSession();
                        final Map<String, String> fileStoreIdsApplicationNoMap = new HashMap<>();
                        if (upadtedWaterConnectionDetails != null) {
                            if (CLOSINGCONNECTION.equals(waterConnectionDetails.getApplicationType().getCode())) {
                                model.addAttribute(FILESTOREIDS,
                                        upadtedWaterConnectionDetails.getClosureFileStore().getFileStoreId());
                                fileStoreIdsApplicationNoMap.put(
                                        upadtedWaterConnectionDetails.getClosureFileStore().getFileStoreId(),
                                        upadtedWaterConnectionDetails.getApplicationNumber());
                            } else if (RECONNECTION.equals(waterConnectionDetails.getApplicationType().getCode())) {
                                model.addAttribute(FILESTOREIDS,
                                        upadtedWaterConnectionDetails.getReconnectionFileStore().getFileStoreId());
                                fileStoreIdsApplicationNoMap.put(
                                        upadtedWaterConnectionDetails.getReconnectionFileStore().getFileStoreId(),
                                        upadtedWaterConnectionDetails.getApplicationNumber());
                            } else {
                                model.addAttribute(FILESTOREIDS,
                                        upadtedWaterConnectionDetails.getFileStore().getFileStoreId());
                                fileStoreIdsApplicationNoMap.put(upadtedWaterConnectionDetails.getFileStore().getFileStoreId(),
                                        upadtedWaterConnectionDetails.getApplicationNumber());
                            }
                            session.setAttribute(APPLICATION_NUMBER, upadtedWaterConnectionDetails.getApplicationNumber());
                        }
                        model.addAttribute("ulbCode", ApplicationThreadLocals.getCityCode());
                        session.setAttribute(MODE, mode);
                        session.setAttribute(APPROVAL_POSITION, approvalPosition);
                        session.setAttribute(APPROVAL_COMMENT, approvalComent);
                        session.setAttribute(FILE_STORE_ID_APPLICATION_NUMBER, fileStoreIdsApplicationNoMap);
                        model.addAttribute("isDigitalSignatureEnabled", waterTaxUtils.isDigitalSignatureEnabled());
                        return "newConnection-digitalSignatureRedirection";
                    }
                waterConnectionDetailsService.updateWaterConnection(waterConnectionDetails, approvalPosition,
                        approvalComent, waterConnectionDetails.getApplicationType().getCode(), workFlowAction,
                        mode, null, sourceChannel);
            } catch (final ValidationException e) {
                throw new ValidationException(e.getMessage());
            }
            if (workFlowAction != null && !workFlowAction.isEmpty()
                    && workFlowAction.equalsIgnoreCase(WF_STATE_BUTTON_GENERATEESTIMATE)) {

                final EstimationNumberGenerator estimationNoGen = beanResolver
                        .getAutoNumberServiceFor(EstimationNumberGenerator.class);

                waterConnectionDetails.setEstimationNumber(estimationNoGen.generateEstimationNumber());
                waterConnectionDetails.setEstimationNoticeDate(new Date());

                final String cityMunicipalityName = cityService.getMunicipalityName();
                final String districtName = cityService.getDistrictName();
                ReportOutput reportOutput = null;
                reportOutput = reportGenerationService.generateEstimationNoticeReport(waterConnectionDetails,
                        cityMunicipalityName, districtName);
                if (reportOutput != null) {
                    final HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.parseMediaType("application/pdf"));
                    headers.add("content-disposition", "inline;filename=EstimationNotice.pdf");
                    String fileName;
                    fileName = SIGNED_DOCUMENT_PREFIX + waterConnectionDetails.getEstimationNumber() + ".pdf";
                    final InputStream fileStream = new ByteArrayInputStream(reportOutput.getReportOutputData());
                    final FileStoreMapper fileStore = fileStoreService.store(fileStream, fileName, "application/pdf",
                            FILESTORE_MODULECODE);
                    waterConnectionDetails.setEstimationNoticeFileStoreId(fileStore);
                    waterConnectionDetailsService.updateWaterConnectionDetailsWithFileStore(waterConnectionDetails);
                }
                return "redirect:/application/estimationNotice?pathVar="
                        + waterConnectionDetails.getApplicationNumber();

            }

            final Assignment currentUserAssignment = assignmentService
                    .getPrimaryAssignmentForGivenRange(securityUtils.getCurrentUser().getId(), new Date(), new Date());
            String nextDesign = "";
            Assignment assignObj = null;
            List<Assignment> asignList = null;
            if (approvalPosition != null)
                assignObj = assignmentService.getPrimaryAssignmentForPositon(approvalPosition);
            if (assignObj != null) {
                asignList = new ArrayList<>();
                asignList.add(assignObj);
            } else if (assignObj == null && approvalPosition != null)
                asignList = assignmentService.getAssignmentsForPosition(approvalPosition, new Date());
            nextDesign = asignList == null || asignList.isEmpty() ? "" : asignList.get(0).getDesignation().getName();

            final String pathVars = waterConnectionDetails.getApplicationNumber() + ","
                    + waterTaxUtils.getApproverName(approvalPosition) + ","
                    + (currentUserAssignment != null ? currentUserAssignment.getDesignation().getName() : "") + ","
                    + (nextDesign != null ? nextDesign : "");
            return "redirect:/application/application-success?pathVars=" + pathVars;
        } else
            return loadViewData(model, request, waterConnectionDetails);
    }

    private ReportOutput getReportOutputObject(final WaterConnectionDetails waterConnectionDetails,
            final String workFlowAction) {
        ReportOutput reportOutput;
        if (waterConnectionDetails.getApplicationType().getCode().equals(CLOSINGCONNECTION))
            reportOutput = reportGenerationService.generateClosureConnectionReport(waterConnectionDetails,
                    workFlowAction);
        else if (waterConnectionDetails.getApplicationType().getCode().equals(RECONNECTION))
            reportOutput = reportGenerationService.generateReconnectionReport(waterConnectionDetails, workFlowAction);
        else if (REGULARIZE_CONNECTION.equalsIgnoreCase(waterConnectionDetails.getApplicationType().getCode()))
            reportOutput = reportGenerationService.generateRegulariseConnProceedings(waterConnectionDetails);
        else
            reportOutput = reportGenerationService.getReportOutput(waterConnectionDetails, workFlowAction);

        return reportOutput;
    }

    private void validateSanctionDetails(final WaterConnectionDetails waterConnectionDetails,
            final BindingResult errors) {

        if (waterConnectionDetails.getApprovalNumber() == null)
            errors.rejectValue("approvalNumber", "approvalNumber.required");

        if (waterConnectionDetails.getApprovalDate() == null)
            errors.rejectValue("approvalDate", "approvalDate.required");
    }

}