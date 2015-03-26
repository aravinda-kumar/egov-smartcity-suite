/*
 * @(#)AppConfigValuesHibernateDAO.java 3.0, 17 Jun, 2013 11:30:16 AM
 * Copyright 2013 eGovernments Foundation. All rights reserved. 
 * eGovernments PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.egov.infstr.config.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.egov.infstr.config.AppConfig;
import org.egov.infstr.config.AppConfigValues;
import org.egov.infstr.dao.GenericHibernateDAO;
import org.egov.infstr.utils.DateUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly=true)
public class AppConfigValuesHibernateDAO extends GenericHibernateDAO<AppConfigValues, Integer> implements AppConfigValuesDAO {

	private static final String KEY_COLUMN_NAME = "keyName";
	private static final String MODULE_COLUMN_NAME = "moduleName";

	@PersistenceContext	
        private EntityManager entityManager;
    
        @Override
        public Session  getCurrentSession() {
                return entityManager.unwrap(Session.class);
        }

	public AppConfigValuesHibernateDAO() {
		super(AppConfigValues.class,null);
	}

	/**
         * Instantiates a new app config values hibernate dao.
         * @param persistentClass the persistent class
         * @param session the session
         */
        public AppConfigValuesHibernateDAO(final Class<AppConfigValues> persistentClass, final Session session) {
                super(persistentClass, session);
        }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AppConfigValues createAppConfigValues(final AppConfigValues appValues) {
		return this.create(appValues);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AppConfigValues> getConfigValuesByModuleAndKey(final String moduleName, final String keyName) {
		final Query qry = getCurrentSession().createQuery("from AppConfigValues a where a.key.keyName =:keyName and a.key.module =:moduleName ");
		qry.setString(KEY_COLUMN_NAME, keyName);
		qry.setString(MODULE_COLUMN_NAME, moduleName);
		return qry.list();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AppConfig> getAppConfigKeys(final String moduleName) {
		final Query qry = getCurrentSession().createQuery("from AppConfig a where a.module =:moduleName ");
		qry.setString(MODULE_COLUMN_NAME, moduleName);
		return qry.list();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AppConfig getConfigKeyByName(final String keyName, final String moduleName) {
		final Query qry = getCurrentSession().createQuery("from AppConfig a where a.keyName =:keyName  and a.module=:moduleName");
		qry.setString(KEY_COLUMN_NAME, keyName);
		qry.setString(MODULE_COLUMN_NAME, moduleName);
		return (AppConfig) qry.uniqueResult();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AppConfigValues getAppConfigValueByDate(final String moduleName, final String keyName, final Date effectiveFrom) {
		final List<AppConfigValues> appConfigValues = getAppConfigValues(moduleName, keyName, effectiveFrom);
		return appConfigValues.isEmpty() ? null : appConfigValues.get(appConfigValues.size() - 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<AppConfigValues> getAppConfigValues(final String moduleName, final String keyName, final Date effectiveFrom) {
		final Query qry = getCurrentSession().createQuery(
				"from AppConfigValues a where a.key.keyName =:keyName and a.key.module =:moduleName and (a.effectiveFrom < :effectiveFrom or a.effectiveFrom between :dateFrom and :dateTo) order by effectiveFrom asc");
		qry.setString(KEY_COLUMN_NAME, keyName);
		qry.setString(MODULE_COLUMN_NAME, moduleName);
		qry.setDate("effectiveFrom", effectiveFrom);
		final Date[] dateRange = DateUtils.constructDateRange(effectiveFrom, effectiveFrom);
		qry.setDate("dateFrom", dateRange[0]);
		qry.setDate("dateTo", dateRange[1]);

		return qry.list();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getAllAppConfigModule() {
		return getCurrentSession().createQuery("select distinct(a.module) from AppConfig a order by a.module").list();
	}
}
