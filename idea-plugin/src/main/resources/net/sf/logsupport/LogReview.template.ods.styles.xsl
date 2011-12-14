<?xml version="1.0" encoding="UTF-8" standalone="yes"?>

<!--
  ~ Copyright 2010, Juergen Kellerer and other contributors.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<xsl:stylesheet version="1.0"
				xmlns:xhtml="http://www.w3.org/1999/xhtml"
				xmlns:xlink="http://www.w3.org/1999/xlink"
				xmlns:dc="http://purl.org/dc/elements/1.1/"
				xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
				xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
				xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
				xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
				xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
				xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
				xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0"
				xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
				xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:variable name="TITLE" select="//xhtml:title/text()"/>

	<xsl:template match="/">
		<office:document-styles>
			<office:font-face-decls>
				<style:font-face style:name="Arial" svg:font-family="Arial"/>
			</office:font-face-decls>
			<office:styles>
				<number:number-style style:name="N0">
					<number:number number:min-integer-digits="1"/>
				</number:number-style>
				<number:text-style style:name="N30">
					<number:text-content/>
				</number:text-style>
				<style:style style:name="Default" style:family="table-cell" style:data-style-name="N0">
					<style:table-cell-properties style:vertical-align="automatic" fo:background-color="transparent"/>
					<style:text-properties fo:color="#000000" style:font-name="Arial" style:font-name-asian="Arial"
										   style:font-name-complex="Arial" fo:font-size="11pt"
										   style:font-size-asian="11pt"
										   style:font-size-complex="11pt"/>
				</style:style>
				<style:default-style style:family="graphic">
					<style:graphic-properties draw:fill="solid" draw:fill-color="#4f81bd" draw:opacity="100%"
											  draw:stroke="solid" svg:stroke-width="0.02778in"
											  svg:stroke-color="#385d8a"
											  svg:stroke-opacity="100%"/>
				</style:default-style>
			</office:styles>
			<office:automatic-styles>
				<style:page-layout style:name="pm1">
					<style:page-layout-properties fo:margin-top="0.31496062992126in"
												  fo:margin-bottom="0.31496062992126in"
												  fo:margin-left="0.236220472440945in"
												  fo:margin-right="0.236220472440945in"
												  style:print-orientation="portrait" style:print-page-order="ttb"
												  style:first-page-number="continue" style:scale-to="90%"
												  style:table-centering="none" style:print="objects charts drawings"/>
					<style:header-style>
						<style:header-footer-properties fo:min-height="0.433070866141732in"
														fo:margin-left="0.236220472440945in"
														fo:margin-right="0.236220472440945in" fo:margin-bottom="0in"/>
					</style:header-style>
					<style:footer-style>
						<style:header-footer-properties fo:min-height="0.433070866141732in"
														fo:margin-left="0.236220472440945in"
														fo:margin-right="0.236220472440945in" fo:margin-top="0in"/>
					</style:footer-style>
				</style:page-layout>
			</office:automatic-styles>
			<office:master-styles>
				<style:master-page style:name="mp1" style:page-layout-name="pm1">
					<style:header>
						<style:region-left>
							<text:p>
								<xsl:value-of select="$TITLE"/>
							</text:p>
						</style:region-left>
						<style:region-right>
							<text:p>Created by Log Support
								<text:s/>
							</text:p>
							<text:p>IntelliJ IDEA Plugin by Juergen Kellerer</text:p>
						</style:region-right>
					</style:header>
					<style:header-left style:display="false"/>
					<style:footer>
						<text:p>Page
							<text:page-number>1</text:page-number>
							<text:s/>of
							<text:s/>
							<text:page-count>99</text:page-count>
						</text:p>
					</style:footer>
					<style:footer-left style:display="false"/>
				</style:master-page>
			</office:master-styles>
		</office:document-styles>
	</xsl:template>
</xsl:stylesheet>