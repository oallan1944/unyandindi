import {
    Alert, Button, Chip, CircularProgress, Dialog, DialogActions, DialogContent,
    DialogContentText, DialogTitle, FormControl, InputLabel, MenuItem, Paper, Select,
    Skeleton, styled, Table, TableBody, TableCell, tableCellClasses, TableContainer,
    TableHead, TableRow,
} from '@mui/material'
import { useEffect, useState } from 'react'
import { useAppDispatch, useAppSelector } from '../../../State/store'
import {
    ACCOUNT_STATUSES, AccountStatus, fetchSellers, setStatusFilter, updateSellerStatus,
} from '../../../State/seller/sellerSlice'
import { Seller } from '../../../types/sellerType'

const statusTitles: Record<AccountStatus, string> = {
    PENDING_VERIFICATION: 'Pending Verification',
    ACTIVE: 'Active',
    SUSPENDED: 'Suspended',
    DEACTIVATED: 'Deactivated',
    BANNED: 'Banned',
    CLOSED: 'Closed',
};

const statusColor: Record<AccountStatus, 'default' | 'success' | 'warning' | 'error'> = {
    PENDING_VERIFICATION: 'warning',
    ACTIVE: 'success',
    SUSPENDED: 'warning',
    DEACTIVATED: 'default',
    BANNED: 'error',
    CLOSED: 'default',
};

const StyledTableCell = styled(TableCell)(({ theme }) => ({
    [`&.${tableCellClasses.head}`]: { backgroundColor: theme.palette.common.black, color: theme.palette.common.white },
    [`&.${tableCellClasses.body}`]: { fontSize: 14 },
}));

const StyledTableRow = styled(TableRow)(({ theme }) => ({
    '&:nth-of-type(odd)': { backgroundColor: theme.palette.action.hover },
    '&:last-child td, &:last-child th': { border: 0 },
}));

const Sellerstable = () => {
    const dispatch = useAppDispatch()
    // Reducer is registered as `seller` (singular) in store.ts's combineReducers.
    const { sellers, loading, error, statusFilter, updatingId } = useAppSelector(
        (state) => state.seller
    )
    const [pendingChange, setPendingChange] = useState<{ seller: Seller; newStatus: AccountStatus } | null>(null)

    useEffect(() => {
        dispatch(fetchSellers(statusFilter))
    }, [dispatch, statusFilter])

    const requestStatusChange = (seller: Seller, newStatus: AccountStatus) => {
        if (newStatus === seller.accountStatus) return
        setPendingChange({ seller, newStatus })
    }

    const confirmStatusChange = () => {
        if (!pendingChange || pendingChange.seller.id === undefined) return
        dispatch(updateSellerStatus({ sellerId: pendingChange.seller.id, accountStatus: pendingChange.newStatus }))
        setPendingChange(null)
    }

    return (
        <>
            <div className='pb-5 w-60'>
                <FormControl fullWidth>
                    <InputLabel id="status-filter-label">Account Status</InputLabel>
                    <Select
                        labelId="status-filter-label"
                        value={statusFilter}
                        label="Account Status"
                        onChange={(e) => dispatch(setStatusFilter(e.target.value))}
                    >
                        <MenuItem value="ALL">All</MenuItem>
                        {ACCOUNT_STATUSES.map((status) => (
                            <MenuItem key={status} value={status}>{statusTitles[status]}</MenuItem>
                        ))}
                    </Select>
                </FormControl>
            </div>

            {error && (
                <Alert severity="error" sx={{ mb: 2 }}>
                    {typeof error === 'string' ? error : 'Something went wrong loading sellers'}
                </Alert>
            )}

            <TableContainer component={Paper}>
                <Table sx={{ minWidth: 700 }} aria-label="sellers table">
                    <TableHead>
                        <TableRow>
                            <StyledTableCell>Seller Name</StyledTableCell>
                            <StyledTableCell>Email</StyledTableCell>
                            <StyledTableCell align="right">Mobile</StyledTableCell>
                            <StyledTableCell align="right">GSTIN</StyledTableCell>
                            <StyledTableCell align="right">Business Name</StyledTableCell>
                            <StyledTableCell align="right">Account Status</StyledTableCell>
                            <StyledTableCell align="right">Change Status</StyledTableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {loading && sellers.length === 0 &&
                            Array.from({ length: 5 }).map((_, i) => (
                                <TableRow key={i}>
                                    {Array.from({ length: 7 }).map((__, j) => (
                                        <StyledTableCell key={j}><Skeleton /></StyledTableCell>
                                    ))}
                                </TableRow>
                            ))}

                        {!loading && sellers.length === 0 && !error && (
                            <TableRow>
                                <StyledTableCell colSpan={7} align="center">No sellers found</StyledTableCell>
                            </TableRow>
                        )}

                        {sellers.map((seller) => {
                            const status = (seller.accountStatus ?? 'PENDING_VERIFICATION') as AccountStatus
                            return (
                                <StyledTableRow key={seller.id}>
                                    <StyledTableCell component="th" scope="row">{seller.sellerName}</StyledTableCell>
                                    <StyledTableCell>{seller.email}</StyledTableCell>
                                    <StyledTableCell align="right">{seller.mobile}</StyledTableCell>
                                    <StyledTableCell align="right">{seller.GSTIN}</StyledTableCell>
                                    <StyledTableCell align="right">{seller.businessDetails?.businessName}</StyledTableCell>
                                    <StyledTableCell align="right">
                                        <Chip
                                            label={statusTitles[status] ?? status}
                                            color={statusColor[status] ?? 'default'}
                                            size="small"
                                        />
                                    </StyledTableCell>
                                    <StyledTableCell align="right">
                                        <FormControl size="small" sx={{ minWidth: 160 }}>
                                            <Select
                                                value={status}
                                                disabled={updatingId === seller.id}
                                                onChange={(e) => requestStatusChange(seller, e.target.value as AccountStatus)}
                                            >
                                                {ACCOUNT_STATUSES.map((s) => (
                                                    <MenuItem key={s} value={s}>{statusTitles[s]}</MenuItem>
                                                ))}
                                            </Select>
                                        </FormControl>
                                        {updatingId === seller.id && <CircularProgress size={16} sx={{ ml: 1 }} />}
                                    </StyledTableCell>
                                </StyledTableRow>
                            )
                        })}
                    </TableBody>
                </Table>
            </TableContainer>

            <Dialog open={!!pendingChange} onClose={() => setPendingChange(null)}>
                <DialogTitle>Confirm status change</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        {pendingChange && (
                            <>Change <strong>{pendingChange.seller.sellerName}</strong>'s status from{' '}
                            <strong>{pendingChange.seller.accountStatus ?? 'PENDING_VERIFICATION'}</strong> to{' '}
                            <strong>{pendingChange.newStatus}</strong>? This takes effect immediately.</>
                        )}
                    </DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setPendingChange(null)}>Cancel</Button>
                    <Button onClick={confirmStatusChange} variant="contained" color="primary">Confirm</Button>
                </DialogActions>
            </Dialog>
        </>
    )
}

export default Sellerstable
