<%--
  ~    eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
  ~    accountability and the service delivery of the government  organizations.
  ~
  ~     Copyright (C) 2017  eGovernments Foundation
  ~
  ~     The updated version of eGov suite of products as by eGovernments Foundation
  ~     is available at http://www.egovernments.org
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU General Public License as published by
  ~     the Free Software Foundation, either version 3 of the License, or
  ~     any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU General Public License for more details.
  ~
  ~     You should have received a copy of the GNU General Public License
  ~     along with this program. If not, see http://www.gnu.org/licenses/ or
  ~     http://www.gnu.org/licenses/gpl.html .
  ~
  ~     In addition to the terms of the GPL license to be adhered to in using this
  ~     program, the following additional terms are to be complied with:
  ~
  ~         1) All versions of this program, verbatim or modified must carry this
  ~            Legal Notice.
  ~            Further, all user interfaces, including but not limited to citizen facing interfaces,
  ~            Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
  ~            derived works should carry eGovernments Foundation logo on the top right corner.
  ~
  ~            For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
  ~            For any further queries on attribution, including queries on brand guidelines,
  ~            please contact contact@egovernments.org
  ~
  ~         2) Any misrepresentation of the origin of the material is prohibited. It
  ~            is required that all modified versions of this material be marked in
  ~            reasonable ways as different from the original version.
  ~
  ~         3) This license does not grant any rights to any user of the program
  ~            with regards to rights under trademark law for use of the trade names
  ~            or trademarks of eGovernments Foundation.
  ~
  ~   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
  ~
  --%>

<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<br />
<c:set value="applicant" var="applicant" scope="request"></c:set>
<form:hidden path="applicant.id" />
<jsp:include page="applicantinfo.jsp">
	<jsp:param value="subheading.applicant.info" name="header" />
</jsp:include>
<br />
	<div class="form-group">
		<div class="col-sm-3 control-label">
			<spring:message code="lbl.fee.criteria"/>
		</div>
		<div class="col-sm-3 view-content">
			 <form:hidden path="feeCriteria.id"  value="${reIssue.feeCriteria.id}"/>	
			 <c:out value="${reIssue.feeCriteria.criteria}" /> 
		</div>
		<div class="col-sm-2 control-label">
			<spring:message code="lbl.fee"/><span class="mandatory"></span>
		</div>
		<div class="col-sm-3 view-content">
			<form:input path="feePaid" value="${feepaid}" id="txt-feepaid" readonly="true" type="text" class="form-control low-width patternvalidation" data-pattern="number" maxlength="4" autocomplete="off" required="required"/>
            <form:errors path="feePaid" cssClass="add-margin error-msg"/>
		</div>
</div>