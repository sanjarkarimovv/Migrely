package com.migrely.app.datasource.exception

/** Google Play billing cannot be used on this phone or in this country (BRD EC-BIL-11). */
class BillingUnavailableDataException : BillingDataException("Google Play billing is unavailable")
