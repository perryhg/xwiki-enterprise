/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
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
package org.xwiki.test.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.xwiki.rest.Relations;
import org.xwiki.test.rest.framework.AbstractHttpTest;
import org.xwiki.rest.model.jaxb.Attachment;
import org.xwiki.rest.model.jaxb.Attachments;
import org.xwiki.rest.model.jaxb.Link;
import org.xwiki.rest.model.jaxb.SearchResult;
import org.xwiki.rest.model.jaxb.SearchResults;
import org.xwiki.rest.model.jaxb.Space;
import org.xwiki.rest.model.jaxb.Spaces;
import org.xwiki.rest.model.jaxb.Wiki;
import org.xwiki.rest.model.jaxb.Wikis;
import org.xwiki.rest.resources.spaces.SpaceAttachmentsResource;
import org.xwiki.rest.resources.spaces.SpaceSearchResource;
import org.xwiki.rest.resources.wikis.WikiSearchResource;
import org.xwiki.rest.resources.wikis.WikisResource;

public class SpacesResourceTest extends AbstractHttpTest
{
    @Override
    public void testRepresentation() throws Exception
    {
        GetMethod getMethod = executeGet(getFullUri(WikisResource.class));
        assertEquals(getHttpMethodInfo(getMethod), HttpStatus.SC_OK, getMethod.getStatusCode());

        Wikis wikis = (Wikis) unmarshaller.unmarshal(getMethod.getResponseBodyAsStream());
        assertTrue(wikis.getWikis().size() > 0);

        Wiki wiki = wikis.getWikis().get(0);
        Link link = getFirstLinkByRelation(wiki, Relations.SPACES);
        assertNotNull(link);

        getMethod = executeGet(link.getHref());
        assertEquals(getHttpMethodInfo(getMethod), HttpStatus.SC_OK, getMethod.getStatusCode());

        Spaces spaces = (Spaces) unmarshaller.unmarshal(getMethod.getResponseBodyAsStream());

        assertTrue(spaces.getSpaces().size() > 0);

        for (Space space : spaces.getSpaces()) {
            link = getFirstLinkByRelation(space, Relations.SEARCH);
            assertNotNull(link);

            checkLinks(space);
        }

    }

    public void testSearch() throws Exception
    {
        GetMethod getMethod =
            executeGet(String.format("%s?q=somethingthatcannotpossiblyexist", getUriBuilder(SpaceSearchResource.class)
                .build(getWiki(), "Main")));
        assertEquals(getHttpMethodInfo(getMethod), HttpStatus.SC_OK, getMethod.getStatusCode());

        SearchResults searchResults = (SearchResults) unmarshaller.unmarshal(getMethod.getResponseBodyAsStream());

        assertEquals(0, searchResults.getSearchResults().size());

        getMethod =
            executeGet(String.format("%s?q=sandbox", getUriBuilder(WikiSearchResource.class)
                .build(getWiki(), "Sandbox")));
        assertEquals(getHttpMethodInfo(getMethod), HttpStatus.SC_OK, getMethod.getStatusCode());

        searchResults = (SearchResults) unmarshaller.unmarshal(getMethod.getResponseBodyAsStream());

        int resultSize = searchResults.getSearchResults().size();
        assertTrue("Found " + resultSize + " result", resultSize >= 2);

        for (SearchResult searchResult : searchResults.getSearchResults()) {
            checkLinks(searchResult);
        }
    }

    public void testAttachments() throws Exception
    {
        GetMethod getMethod =
            executeGet(String.format("%s", getUriBuilder(SpaceAttachmentsResource.class).build(getWiki(), "Main")));
        assertEquals(getHttpMethodInfo(getMethod), HttpStatus.SC_OK, getMethod.getStatusCode());

        Attachments attachments = (Attachments) unmarshaller.unmarshal(getMethod.getResponseBodyAsStream());

        assertEquals(getAttachmentsInfo(attachments), 2, attachments.getAttachments().size());

        for (Attachment attachment : attachments.getAttachments()) {
            checkLinks(attachment);
        }

    }
}