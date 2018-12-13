
select distinct o.title, o.creator_corporate, o.subject_swd, o.description,
        o.publisher_university, o.contributors_name, o.contributors_corporate,
        o.date_year, o.date_creation,
        from_unixtime(o.date_creation, '%Y-%m-%d') date_creation_esc,
        o.date_modified,
        from_unixtime(o.date_modified,'%Y-%m-%d') date_modified_esc,
        r.type, r.dini_publtype,
        o.source_opus, o.source_title, o.source_swb, o.language,
        o.verification, o.subject_uncontrolled_german,
        o.subject_uncontrolled_english, o.title_en, o.description2,
        o.subject_type, o.date_valid,
        from_unixtime(o.date_valid,'%Y-%m-%d') date_valid_esc,
        o.description_lang, o.description2_lang, o.isbn, o.sachgruppe_ddc,
        o.lic, lic.link as license, d.date_accepted, 
        from_unixtime(d.date_accepted,'%Y-%m-%d') date_accepted_esc,
        d.advisor, d.title_de, f.name faculty_name, d.publisher_faculty,
        i.inst_nr, inst.name inst_name, s.sachgruppe, s.sachgruppe_en,
        domain.ip as accessRights
 from opus o -- left join opus_autor a on o.source_opus=a.source_opus
        left join resource_type r on r.typeid = o.type
        left join opus_diss d on o.source_opus=d.source_opus
        left join opus_inst i on o.source_opus=i.source_opus
        left join faculty f on f.nr=d.publisher_faculty
        left join institute inst on i.inst_nr=inst.nr
        left join sachgruppe_ddc_de s on o.sachgruppe_ddc=s.nr
        left join license_de lic on o.lic=lic.shortname
        left join domain on o.bereich_id=domain.id and domain.ip is not null
 where o.source_opus=<oid>
 limit 1
 ;
 select creator_name,reihenfolge,gnd,orcid from opus_autor 
 where source_opus=<oid> order by reihenfolge
 ; 
 select o.class as pacs, p.bez as pacs_name 
     from opus_pacs o left join pacs2003 p
     on left(o.class,8) = left(p.class,8)
     where source_opus=<oid>
 ;
 select o.class as msc2000, m.bez msc_name from opus_msc o left join msc2000 m
        on left(o.class,5) = left(m.class,5)
        where o.source_opus=<oid>

 ;
 -- isPartOf
 select o.sr_id, o.sequence_nr, s.name, s.url, s.urn
         from opus_schriftenreihe o, schriftenreihen s
         where source_opus=<oid>
         and o.sr_id=s.sr_id
 ;
 -- statistics
 select source_opus, uri, urn, doi, stat, ppn from statistics where source_opus=<oid>
 ;
 -- graph
 select d.url, d.longname from domain d, opus o where d.id=o.bereich_id and o.source_opus=<oid>
 ;
 -- files
 select file,extent from files where files.source_opus=<oid>
 ;

 -- toc table of contents
 select seq, label, page, number from opus_toc where source_opus=<oid>  

 ;
 -- references
 select source_opus, uri, title, date, cite, authors from sv_references 
 where source_opus=<oid>
 -- -------------------------------------------------------------------------
