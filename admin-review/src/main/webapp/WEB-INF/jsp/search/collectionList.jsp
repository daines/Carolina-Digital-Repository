<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:forEach items="${resultResponse.resultList}" var="metadata" varStatus="status">
	<div id="entry${metadata.id}" class="browseitem">
		<div class="contentarea">
			<c:set var="thumbnailUrl" value="${metadata.getDatastream('THUMB_LARGE')}"/>
			<a href="/record?id=${metadata.id}" target="_blank">
				<c:choose>
					<c:when test="${not empty thumbnailUrl}"><img class="largethumb" src="" /></c:when>
					<c:otherwise><img class="largethumb" src="/static/images/placeholder/large/oldwell.jpg" /></c:otherwise>
				</c:choose>
			</a>

			<ul class="itemnavigation">
				<li>
					<c:set var="unpublishedCount" value="${metadata.countMap.unpublished}"/>
					<c:choose>
						<c:when test="${not empty unpublishedCount}">
							<a href="review.html">Review ${unpublishedCount} unpublished item<c:if test="${unpublishedCount != 1}">s</c:if></a>
						</c:when>
						<c:otherwise>No unpublished items</c:otherwise>
					</c:choose>
				</li>
			</ul>

			<div class="itemdetails">
				<h2>
					<a href="record?id=${metadata.id}" target="_blank"
						class="has_tooltip" title="View details for <c:out value='${metadata.title}'/>."><c:out value='${metadata.title}'/></a>
					<c:set var="childCount" value="${metadata.countMap.child}"/>
					<span class="searchitem_container_count">
						<c:choose>
							<c:when test="${not empty childCount}">
								<a href="review.html">(${childCount} item<c:if test="${childCount != 1}">s</c:if>)</a>
							</c:when>
							<c:otherwise>(0 items)</c:otherwise>
						</c:choose>
					</span>
				</h2>
				<p>Last Updated: <c:out value='${metadata.dateUpdated}'/></p>
				<c:if test="${not empty metadata.abstractText}">
					<p>
						<c:out value="${metadata.abstractText}"/>
					</p>
				</c:if>
			</div>
		</div>
	</div>
</c:forEach>