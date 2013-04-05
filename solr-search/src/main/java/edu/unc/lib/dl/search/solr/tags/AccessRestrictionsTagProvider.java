package edu.unc.lib.dl.search.solr.tags;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import edu.unc.lib.dl.acl.util.AccessGroupSet;
import edu.unc.lib.dl.acl.util.UserRole;
import edu.unc.lib.dl.search.solr.model.BriefObjectMetadata;
import edu.unc.lib.dl.search.solr.model.Tag;

public class AccessRestrictionsTagProvider implements TagProvider {
	private static final String[] PUBLIC = new String[] { "public" };

	@Override
	public void addTags(BriefObjectMetadata record, AccessGroupSet accessGroups) {
		// public
		Set<UserRole> publicRoles = record.getAccessControlBean().getRoles(
				PUBLIC);
		if (publicRoles.contains(UserRole.patron)) {
			record.addTag(new Tag("public",
					"The public has access to this object."));
		} else if (publicRoles.contains(UserRole.metadataPatron)) {
			record.addTag(new Tag("public",
					"The public has access to this object's metadata."));
		} else if (publicRoles.contains(UserRole.accessCopiesPatron)) {
			record.addTag(new Tag("public",
					"This public has access to this object's metadata and access copies."));
		}

		// unpublished
		if (record.getStatus().contains("Unpublished")) {
			record.addTag(new Tag("unpublished",
					"This object is not published."));
		}

		if (accessGroups != null) {
			Set<UserRole> myRoles = record.getAccessControlBean().getRoles(
					accessGroups);

			// view only
			if (myRoles.contains(UserRole.observer)) {
				record.addTag(new Tag("view only",
						"You are an observer of this object."));
			}
		}

		// embargo
		Date lastEmbargo = record.getAccessControlBean()
				.getLastActiveEmbargoUntilDate();
		if (lastEmbargo != null) {
			String text = new StringBuilder("This object is embargoed until ")
					.append(DateFormat.getDateInstance().format(lastEmbargo))
					.toString();
			record.addTag(new Tag("embargoed", text));
		}

		// described (separate DescriptiveTagProvider)
	}
}
