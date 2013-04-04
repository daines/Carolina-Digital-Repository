<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page trimDirectiveWhitespaces="true" %>

<link rel="stylesheet" type="text/css" href="/static/css/admin/admin_forms.css" />
<link rel="stylesheet" type="text/css" href="/static/css/admin/jqueryui-editable.css" />

<%
	// Retrieving the static CDR ACL Namespace object
	pageContext.setAttribute("aclNS", edu.unc.lib.dl.xml.JDOMNamespaceUtil.CDR_ACL_NS);
%>

<div class="edit_acls">
	<h3>Access Settings</h3>
	<div class="form_field">
		<label>Published</label>
		<c:set var="publishedValue" value="${targetACLs.getAttributeValue('published', aclNS)}" />
		<a class="boolean_toggle">
			<c:choose>
				<c:when test="${publishedValue == 'false'}">No</c:when>
				<c:otherwise>Yes</c:otherwise>
			</c:choose>
		</a>
		<c:if test="${targetMetadata.status.contains('Parent Unpublished')}">
			&nbsp;(Parent is unpublished)
		</c:if>
	</div>
	<div class="form_field">
		<label>Embargo</label>
		<c:set var="embargoValue" value="${targetACLs.getAttributeValue('embargo-until', aclNS)}" />
		<a href="#" class="add_embargo" data-type="combodate">
			<c:if test="${embargoValue != null}">
				<c:out value="${embargoValue}" />
			</c:if>
		</a>
	</div>
	<div class="form_field">
		<label>Discoverable</label>
		<c:set var="discoverableValue" value="${targetACLs.getAttributeValue('discoverable', aclNS)}" />
		<a class="boolean_toggle">
			<c:choose>
				<c:when test="${discoverableValue == 'false'}">
					No
				</c:when>
				<c:otherwise>
					Yes
				</c:otherwise>
			</c:choose>
		</a>
	</div>
	<h3>Roles Granted</h3>
	<div class="form_field">
		<label>Inherit from parents?</label>
		<c:set var="inheritValue" value="${targetACLs.getAttributeValue('inherit', aclNS)}" />
		<a class="boolean_toggle">
			<c:choose>
				<c:when test="${inheritValue == 'false'}">
					No
					<c:set var="inheritanceDisabled" value="inheritance_disabled" />
				</c:when>
				<c:otherwise>
					Yes
				</c:otherwise>
			</c:choose>
		</a>
	</div>
	<div class="clear"></div>
	<table class="roles_granted  ${inheritanceDisabled}">
		<c:forEach items="${rolesGranted}" var="roleEntry">
			<tr class="role_${roleEntry.key}">
				<td class="role">${roleEntry.key}</td>
				<td class="groups">
					<c:forEach items="${roleEntry.value}" var="groupEntry">
						<c:choose>
							<c:when test="${groupEntry.inherited}">
								<span class="inherited">${groupEntry.roleName}</span>
							</c:when>
							<c:otherwise>
								<span>${groupEntry.roleName}</span><a class="remove_group">x</a>
							</c:otherwise>
						</c:choose>
						<br/>
					</c:forEach>
				</td>
			</tr>
		</c:forEach>
		<!-- 
		<tr class="role_curator">
			<td class="role">curator</td>
			<td id="groups">
				<span>unc:app:lib:cdr:curator</span><a class="remove_group">x</a><br/>
				<span>unc:app:lib:cdr:SILS</span><a class="remove_group">x</a><br/>
				<span class="inherited" title="Group granted role by a parent container.  To remove, disable 'Inherit from parents' on this object or remove the granted role from its parent.">unc:app:lib:cdr:admin</span><br/>
			</td>
		</tr>
		<tr class="role_patron">
			<td class="role">patron</td>
			<td class="groups">
				<span class="inherited" title="Group granted role by a parent container.  To remove, disable 'Inherit from parents' on this object or remove the granted role from its parent.">authenticated</span><br/>
			</td>
		</tr>
		<tr class="role_patron">
			<td class="role">access copies patron</td>
			<td class="groups">
				<span class="inherited" title="Group granted role by a parent container.  To remove, disable 'Inherit from parents' on this object or remove the granted role from its parent.">public</span><br/>
			</td>
		</tr> -->
		<tr class="edit_role_granted">
			<td>
			</td>
			<td>
				<a>Edit</a>
			</td>
		</tr>
		<tr class="add_role_granted">
			<td class="role">
				<select class="add_role_name">
					<option value="">Role</option>
					<option value="metadata-patron">Metadata Patron</option>
					<option value="access-copies-patron">Access Copies Patron</option>
					<option value="patron">Patron</option>
					<option value="observer">Observer</option>
					<option value="ingester">Ingester</option>
					<option value="processor">Processor</option>
					<option value="curator">Curator</option>
				</select>
			</td>
			<td>
				<input type="text" placeholder="group" value="" class="add_group_name"/>
				<input type="button" value="Add role" class="add_role_button"/>
			</td>
		</tr>
	</table>
	<div class="update_field">
		<input type="button" value="Update" class="update_button"/>
	</div>
</div>
